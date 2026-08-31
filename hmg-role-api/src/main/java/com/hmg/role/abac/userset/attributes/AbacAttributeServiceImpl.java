package com.hmg.role.abac.userset.attributes;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.common.exceptions.AbacAttributeNotFoundException;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDeleteDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeSearchDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionConflictDetailDto;
import com.hmg.role.abac.userset.attributes.exceptions.ConditionAttributeStillInUseException;
import com.hmg.role.abac.userset.attributes.exceptions.OperandTypeDuplicateException;
import com.hmg.role.abac.userset.attributes.exceptions.OperandTypeInvalidException;
import com.hmg.role.admin.project.Project;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AbacAttributeServiceImpl implements AbacAttributeService {
    private final ConditionOperandRepository repository;

    private final ConditionOperandMapper mapper;

    @Setter(onMethod_ = {@Autowired, @Lazy})
    private AuthorRequestScope authorRequestScope;

    @Override
    public ListResponseDto<ConditionAttributeDto> getAll(
            ConditionAttributeSearchDto params, OperandSubject subject) {
        Project project = authorRequestScope.getProject();

        String keyLike = params.getKeyLike();
        Pageable pageable = params.pageRequest();

        Specification<ConditionOperand> spec =
                ConditionOperandSpecification.findByKeyLikeAndProjectAndDeletedFalse(
                        keyLike, OperandType.ATTRIBUTE, subject, project);

        var entities = repository.findAll(spec, pageable);

        var dtos = entities.stream().map(mapper::toDto).toList();

        var page = new PageImpl<>(dtos, params.pageRequest(), entities.getTotalElements());
        return ListResponseDto.create(page);
    }

    @Override
    public ConditionAttributeDto create(ConditionAttributeDto dto, OperandSubject subject) {
        Project project = authorRequestScope.getProject();
        validateCreateDto(dto, subject, project);

        ConditionOperand entity = mapper.toEntity(dto);
        entity.setProject(project);
        entity.setSubject(subject);

        entity = repository.save(entity);

        return mapper.toDto(entity);
    }

    private void validateCreateDto(
            ConditionAttributeDto dto, OperandSubject subject, Project project) {
        if (dto.type() != OperandType.ATTRIBUTE) {
            throw new OperandTypeInvalidException(dto.key(), dto.dataType());
        }

        String key = dto.key();
        Specification<ConditionOperand> spec =
                ConditionOperandSpecification.keyExists(key, dto.type(), subject, project);
        boolean exists = repository.exists(spec);
        if (exists) {
            throw new OperandTypeDuplicateException(key, subject);
        }
    }

    @Override
    public void delete(ConditionAttributeDeleteDto dto, OperandSubject subject) {
        Project project = authorRequestScope.getProject();
        String key = dto.key();

        // find out if it is still in use
        var conflicts =
                switch (subject) {
                    case OperandSubject.USER_SET ->
                            repository.findUserSetsUsingAttribute(key, project);
                    case OperandSubject.RESOURCE_SET ->
                            repository.findResourceSetsUsingAttribute(key, project);
                };
        if (conflicts.isEmpty()) {
            // no attribute is using it; it could safely be deleted
            repository.deleteByOperandAndTypeAndSubjectAndProject(
                    key, OperandType.ATTRIBUTE, subject, project);
        } else {
            // attribute still in use
            throw new ConditionAttributeStillInUseException(
                    new ConditionConflictDetailDto(conflicts));
        }
    }

    @Override
    public ConditionOperand getOrCreateOperand(
            OperandSubject subject,
            String operand,
            OperandType type,
            OperandDataType dataType,
            Project project) {

        return switch (type) {
            case OperandType.ATTRIBUTE -> {
                // this attribute must exist and valid
                Optional<ConditionOperand> dbEntity =
                        repository.findByProjectAndOperandAndTypeAndSubjectAndDeletedFalse(
                                project, operand, type, subject);
                if (dbEntity.isEmpty()) {
                    throw new AbacAttributeNotFoundException(subject, operand, project.getKey());
                } else {
                    yield dbEntity.get();
                }
            }
            case OperandType.LITERAL -> {
                // Lookup must include dataType: a STRING literal "222" and a NUMBER literal "222"
                // are distinct entities. Without dataType in the key, the second condition reuses
                // the first's ConditionOperand (wrong dataType), producing a broken SpEL
                // expression.
                Optional<ConditionOperand> dbEntity =
                        repository
                                .findByProjectAndOperandAndTypeAndDataTypeAndSubjectAndDeletedFalse(
                                        project, operand, type, dataType, subject);
                if (dbEntity.isEmpty()) {
                    // the literal doesn't exist in db; create it
                    ConditionOperand mapped =
                            mapper.toConditionOperand(subject, operand, type, dataType, project);
                    yield repository.save(mapped);
                } else {
                    // reuse existing literal
                    yield dbEntity.get();
                }
            }
        };
    }

    @Override
    public void opportunisticDeleteLiterals(
            Collection<ConditionOperand> conditionOperands,
            OperandSubject subject,
            Project project) {
        // Filter for literals only and opportunistically delete each one
        conditionOperands.stream()
                .filter(co -> co.getType() == OperandType.LITERAL)
                .forEach(
                        literal ->
                                opportunisticDeleteLiteral(
                                        literal.getOperand(),
                                        literal.getDataType(),
                                        subject,
                                        project));
    }

    @Override
    public void opportunisticDelete(
            String operand, OperandType type, OperandSubject subject, Project project) {
        boolean isStillInUse = getCheckStillInUse(operand, type, subject, project);
        if (isStillInUse) {
            // operand is still in use, do nothing
            return;
        }

        // operand is no longer used, delete it
        log.info(
                "Attribute: {} ({}) of {} in project {} is no longer in use, will delete",
                operand,
                type,
                subject,
                project.getKey());
        repository.deleteByOperandAndTypeAndSubjectAndProject(operand, type, subject, project);
    }

    private void opportunisticDeleteLiteral(
            String operand, OperandDataType dataType, OperandSubject subject, Project project) {
        boolean isStillInUse = getCheckStillInUseLiteral(operand, dataType, subject, project);
        if (isStillInUse) {
            return;
        }

        log.info(
                "Literal: {} ({}) of {} in project {} is no longer in use, will delete",
                operand,
                dataType,
                subject,
                project.getKey());
        repository.deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                operand, OperandType.LITERAL, dataType, subject, project);
    }

    private boolean getCheckStillInUse(
            String key, OperandType type, OperandSubject subject, Project project) {
        return switch (subject) {
            case OperandSubject.USER_SET -> repository.checkStillInUseInUserSet(key, type, project);
            case OperandSubject.RESOURCE_SET ->
                    repository.checkStillInUseInResourceSet(key, type, project);
        };
    }

    private boolean getCheckStillInUseLiteral(
            String key, OperandDataType dataType, OperandSubject subject, Project project) {
        return switch (subject) {
            case OperandSubject.USER_SET ->
                    repository.checkStillInUseInUserSet(
                            key, OperandType.LITERAL, dataType, project);
            case OperandSubject.RESOURCE_SET ->
                    repository.checkStillInUseInResourceSet(
                            key, OperandType.LITERAL, dataType, project);
        };
    }
}
