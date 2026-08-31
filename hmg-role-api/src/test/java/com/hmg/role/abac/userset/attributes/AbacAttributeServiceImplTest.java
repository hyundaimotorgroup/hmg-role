package com.hmg.role.abac.userset.attributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hmg.role.abac.common.enums.OperandSubject;
import com.hmg.role.abac.userset.attributes.dto.ConditionAttributeDeleteDto;
import com.hmg.role.abac.userset.attributes.dto.ConditionConflictDto;
import com.hmg.role.abac.userset.attributes.exceptions.ConditionAttributeStillInUseException;
import com.hmg.role.admin.project.Project;
import com.hmg.role.util.AuthorRequestScope;
import com.hmg.role.util.enums.OperandDataType;
import com.hmg.role.util.enums.OperandType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbacAttributeServiceImplTest {

    @Mock private ConditionOperandRepository repository;
    @Mock private ConditionOperandMapper mapper;
    @Mock private AuthorRequestScope authorRequestScope;

    @InjectMocks private AbacAttributeServiceImpl service;

    private Project project;

    @BeforeEach
    void setUp() {
        service.setAuthorRequestScope(authorRequestScope);

        project = new Project();
        project.setId(1L);
        project.setKey("proj-1");
    }

    private ConditionOperand literal(String operand) {
        return literal(operand, OperandDataType.STRING);
    }

    private ConditionOperand literal(String operand, OperandDataType dataType) {
        var co = new ConditionOperand();
        co.setOperand(operand);
        co.setType(OperandType.LITERAL);
        co.setSubject(OperandSubject.USER_SET);
        co.setDataType(dataType);
        co.setProject(project);
        return co;
    }

    private ConditionOperand attribute(String operand) {
        var co = new ConditionOperand();
        co.setOperand(operand);
        co.setType(OperandType.ATTRIBUTE);
        co.setSubject(OperandSubject.USER_SET);
        co.setDataType(OperandDataType.STRING);
        co.setProject(project);
        return co;
    }

    // ==================== opportunisticDeleteLiterals ====================

    @Test
    void opportunisticDeleteLiterals_emptyList_noRepositoryInteraction() {
        service.opportunisticDeleteLiterals(List.of(), OperandSubject.USER_SET, project);
        verifyNoInteractions(repository);
    }

    @Test
    void opportunisticDeleteLiterals_onlyAttributes_skipsAll() {
        service.opportunisticDeleteLiterals(
                List.of(attribute("emp-id")), OperandSubject.USER_SET, project);

        verify(repository, never())
                .checkStillInUseInUserSet(any(), any(), any(OperandDataType.class), any());
        verify(repository, never())
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        any(), any(), any(), any(), any());
    }

    @Test
    void opportunisticDeleteLiterals_literalNotInUse_deletesWithLiteralTypeAndDataTypeFilter() {
        when(repository.checkStillInUseInUserSet(
                        "manager", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(false);

        service.opportunisticDeleteLiterals(
                List.of(literal("manager")), OperandSubject.USER_SET, project);

        verify(repository)
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        "manager",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET,
                        project);
    }

    @Test
    void opportunisticDeleteLiterals_literalStillInUse_doesNotDelete() {
        when(repository.checkStillInUseInUserSet(
                        "manager", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(true);

        service.opportunisticDeleteLiterals(
                List.of(literal("manager")), OperandSubject.USER_SET, project);

        verify(repository, never())
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        any(), any(), any(), any(), any());
    }

    @Test
    void opportunisticDeleteLiterals_attributeWithSameNameInUse_deletesLiteralAnyway() {
        when(repository.checkStillInUseInUserSet(
                        "emp-id", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(false);

        service.opportunisticDeleteLiterals(
                List.of(literal("emp-id")), OperandSubject.USER_SET, project);

        verify(repository)
                .checkStillInUseInUserSet(
                        "emp-id", OperandType.LITERAL, OperandDataType.STRING, project);
        verify(repository)
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        "emp-id",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET,
                        project);
    }

    @Test
    void opportunisticDeleteLiterals_mixedOperands_onlyDeletesUnusedLiterals() {
        var unusedLit = literal("val-a");
        var inUseLit = literal("val-b");
        var attr = attribute("some-attr");

        when(repository.checkStillInUseInUserSet(
                        "val-a", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(false);
        when(repository.checkStillInUseInUserSet(
                        "val-b", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(true);

        service.opportunisticDeleteLiterals(
                List.of(unusedLit, inUseLit, attr), OperandSubject.USER_SET, project);

        verify(repository)
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        "val-a",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET,
                        project);
        verify(repository, never())
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        eq("val-b"), any(), any(), any(), any());
        verify(repository, never())
                .checkStillInUseInUserSet(
                        eq("some-attr"), any(), any(OperandDataType.class), any());
    }

    @Test
    void opportunisticDeleteLiterals_resourceSetSubject_usesResourceSetCheckAndDelete() {
        var lit = new ConditionOperand();
        lit.setOperand("res-val");
        lit.setType(OperandType.LITERAL);
        lit.setSubject(OperandSubject.RESOURCE_SET);
        lit.setDataType(OperandDataType.STRING);
        lit.setProject(project);

        when(repository.checkStillInUseInResourceSet(
                        "res-val", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(false);

        service.opportunisticDeleteLiterals(List.of(lit), OperandSubject.RESOURCE_SET, project);

        verify(repository)
                .checkStillInUseInResourceSet(
                        "res-val", OperandType.LITERAL, OperandDataType.STRING, project);
        verify(repository)
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        "res-val",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.RESOURCE_SET,
                        project);
    }

    // ==================== opportunisticDeleteLiterals: same value, different dataType
    // ====================

    @Test
    void opportunisticDeleteLiterals_sameLiteralValueDifferentDataType_onlyDeletesTargetedOne() {
        // Scenario: Number002=222 and String002=222. When deleting the STRING "222" literal
        // (e.g. after removing String002's condition), the NUMBER "222" literal must survive even
        // if it happens to still be in use — the two are distinct ConditionOperand rows.
        var stringLit = literal("222", OperandDataType.STRING);

        // STRING "222" is no longer referenced
        when(repository.checkStillInUseInUserSet(
                        "222", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(false);

        service.opportunisticDeleteLiterals(List.of(stringLit), OperandSubject.USER_SET, project);

        // Must delete only the STRING typed literal
        verify(repository)
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        "222",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET,
                        project);
        // Must NOT touch the NUMBER typed literal
        verify(repository, never())
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        eq("222"), any(), eq(OperandDataType.NUMBER), any(), any());
        verify(repository, never())
                .checkStillInUseInUserSet(eq("222"), any(), eq(OperandDataType.NUMBER), any());
    }

    @Test
    void opportunisticDeleteLiterals_sameLiteralValueNumberInUse_doesNotDeleteStringLiteral() {
        // When the NUMBER "222" literal is still in use, it must not block deletion of
        // the STRING "222" literal, which is checked independently.
        var stringLit = literal("222", OperandDataType.STRING);
        var numberLit = literal("222", OperandDataType.NUMBER);

        when(repository.checkStillInUseInUserSet(
                        "222", OperandType.LITERAL, OperandDataType.STRING, project))
                .thenReturn(false); // STRING "222" unused — should be deleted
        when(repository.checkStillInUseInUserSet(
                        "222", OperandType.LITERAL, OperandDataType.NUMBER, project))
                .thenReturn(true); // NUMBER "222" still in use — must NOT be deleted

        service.opportunisticDeleteLiterals(
                List.of(stringLit, numberLit), OperandSubject.USER_SET, project);

        // Only the STRING literal is deleted
        verify(repository)
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        "222",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET,
                        project);
        verify(repository, never())
                .deleteByOperandAndTypeAndDataTypeAndSubjectAndProject(
                        eq("222"), any(), eq(OperandDataType.NUMBER), any(), any());
    }

    // ==================== getOrCreateOperand (LITERAL) ====================

    @Test
    void getOrCreateOperand_literal_notInDb_createsAndSaves() {
        when(repository.findByProjectAndOperandAndTypeAndDataTypeAndSubjectAndDeletedFalse(
                        project,
                        "admin",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET))
                .thenReturn(java.util.Optional.empty());
        var newEntity = literal("admin", OperandDataType.STRING);
        when(mapper.toConditionOperand(
                        OperandSubject.USER_SET,
                        "admin",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        project))
                .thenReturn(newEntity);
        when(repository.save(newEntity)).thenReturn(newEntity);

        var result =
                service.getOrCreateOperand(
                        OperandSubject.USER_SET,
                        "admin",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        project);

        assertSame(newEntity, result);
        verify(repository).save(newEntity);
    }

    @Test
    void getOrCreateOperand_literal_existsInDb_reusesWithoutCreating() {
        var existing = literal("admin", OperandDataType.STRING);
        when(repository.findByProjectAndOperandAndTypeAndDataTypeAndSubjectAndDeletedFalse(
                        project,
                        "admin",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET))
                .thenReturn(java.util.Optional.of(existing));

        var result =
                service.getOrCreateOperand(
                        OperandSubject.USER_SET,
                        "admin",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        project);

        assertSame(existing, result);
        verify(repository, never()).save(any());
    }

    @Test
    void getOrCreateOperand_literal_sameValueDifferentDataType_createsNewRow() {
        // Root-cause regression: "222" NUMBER already exists. "222" STRING must not reuse it.
        var numberLit = literal("222", OperandDataType.NUMBER);
        var newStringLit = literal("222", OperandDataType.STRING);

        // NUMBER lookup returns the existing entity (irrelevant here — different call)
        // STRING lookup returns empty — no STRING "222" exists yet
        when(repository.findByProjectAndOperandAndTypeAndDataTypeAndSubjectAndDeletedFalse(
                        project,
                        "222",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        OperandSubject.USER_SET))
                .thenReturn(java.util.Optional.empty());
        when(mapper.toConditionOperand(
                        OperandSubject.USER_SET,
                        "222",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        project))
                .thenReturn(newStringLit);
        when(repository.save(newStringLit)).thenReturn(newStringLit);

        var result =
                service.getOrCreateOperand(
                        OperandSubject.USER_SET,
                        "222",
                        OperandType.LITERAL,
                        OperandDataType.STRING,
                        project);

        // Must return a distinct STRING entity, not the NUMBER one
        assertSame(newStringLit, result);
        assertNotSame(numberLit, result);
        verify(repository).save(newStringLit);
    }

    // ==================== opportunisticDelete ====================

    @Test
    void opportunisticDelete_literalNotInUseUserSet_deletesWithTypeFilter() {
        when(repository.checkStillInUseInUserSet("val", OperandType.LITERAL, project))
                .thenReturn(false);

        service.opportunisticDelete("val", OperandType.LITERAL, OperandSubject.USER_SET, project);

        verify(repository)
                .deleteByOperandAndTypeAndSubjectAndProject(
                        "val", OperandType.LITERAL, OperandSubject.USER_SET, project);
    }

    @Test
    void opportunisticDelete_literalStillInUse_doesNotDelete() {
        when(repository.checkStillInUseInUserSet("val", OperandType.LITERAL, project))
                .thenReturn(true);

        service.opportunisticDelete("val", OperandType.LITERAL, OperandSubject.USER_SET, project);

        verify(repository, never())
                .deleteByOperandAndTypeAndSubjectAndProject(any(), any(), any(), any());
    }

    @Test
    void opportunisticDelete_literalNotInUseResourceSet_deletesWithTypeFilter() {
        when(repository.checkStillInUseInResourceSet("r-val", OperandType.LITERAL, project))
                .thenReturn(false);

        service.opportunisticDelete(
                "r-val", OperandType.LITERAL, OperandSubject.RESOURCE_SET, project);

        verify(repository)
                .deleteByOperandAndTypeAndSubjectAndProject(
                        "r-val", OperandType.LITERAL, OperandSubject.RESOURCE_SET, project);
    }

    // ==================== delete (explicit ATTRIBUTE deletion) ====================

    @Test
    void delete_userSetAttributeNotInUse_deletesOnlyAttributeType() {
        when(authorRequestScope.getProject()).thenReturn(project);
        when(repository.findUserSetsUsingAttribute("emp-id", project)).thenReturn(List.of());

        service.delete(new ConditionAttributeDeleteDto("emp-id"), OperandSubject.USER_SET);

        // Must use the type-scoped delete to avoid accidentally deleting a LITERAL
        // with the same operand name
        verify(repository)
                .deleteByOperandAndTypeAndSubjectAndProject(
                        "emp-id", OperandType.ATTRIBUTE, OperandSubject.USER_SET, project);
    }

    @Test
    void delete_userSetAttributeStillInUse_throwsAndDoesNotDelete() {
        var conflict = new ConditionConflictDto("us-1", "User Set One");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(repository.findUserSetsUsingAttribute("emp-id", project))
                .thenReturn(List.of(conflict));

        assertThrows(
                ConditionAttributeStillInUseException.class,
                () ->
                        service.delete(
                                new ConditionAttributeDeleteDto("emp-id"),
                                OperandSubject.USER_SET));

        verify(repository, never())
                .deleteByOperandAndTypeAndSubjectAndProject(any(), any(), any(), any());
    }

    @Test
    void delete_resourceSetAttributeNotInUse_deletesOnlyAttributeType() {
        when(authorRequestScope.getProject()).thenReturn(project);
        when(repository.findResourceSetsUsingAttribute("res-attr", project)).thenReturn(List.of());

        service.delete(new ConditionAttributeDeleteDto("res-attr"), OperandSubject.RESOURCE_SET);

        verify(repository)
                .deleteByOperandAndTypeAndSubjectAndProject(
                        "res-attr", OperandType.ATTRIBUTE, OperandSubject.RESOURCE_SET, project);
    }

    @Test
    void delete_resourceSetAttributeStillInUse_throwsAndDoesNotDelete() {
        var conflict = new ConditionConflictDto("rs-1", "Resource Set One");

        when(authorRequestScope.getProject()).thenReturn(project);
        when(repository.findResourceSetsUsingAttribute("res-attr", project))
                .thenReturn(List.of(conflict));

        assertThrows(
                ConditionAttributeStillInUseException.class,
                () ->
                        service.delete(
                                new ConditionAttributeDeleteDto("res-attr"),
                                OperandSubject.RESOURCE_SET));

        verify(repository, never())
                .deleteByOperandAndTypeAndSubjectAndProject(any(), any(), any(), any());
    }
}
