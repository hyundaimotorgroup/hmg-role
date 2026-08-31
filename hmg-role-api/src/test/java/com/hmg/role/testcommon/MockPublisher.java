package com.hmg.role.testcommon;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/** Mock class for Spring's ApplicationEventPublisher to run unit tests that doesn't use Cucumber */
@Getter
public class MockPublisher implements ApplicationEventPublisher {
    private final List<ApplicationEvent> applicationEvents;
    private final List<Object> objects;

    private long invocationCount;

    public MockPublisher() {
        applicationEvents = new LinkedList<>();
        objects = new LinkedList<>();
        invocationCount = 0;
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        ApplicationEventPublisher.super.publishEvent(event);
        applicationEvents.add(event);
        invocationCount++;
        System.out.println("Captured event: " + event);
    }

    @Override
    public void publishEvent(Object event) {
        objects.add(event);
        invocationCount++;
        System.out.println("Captured event: " + event);
    }

    public List getAll() {
        LinkedList ans = new LinkedList();
        ans.addAll(applicationEvents);
        ans.addAll(objects);
        return ans;
    }
}
