package ru.yandex.qatools.allure.events;

import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.Step;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 11.11.13
 */
public class StepSkippedEvent extends AbstractStepSkippedEvent {

    @Override
    public void process(Step step) {
        step.setStatus(Status.SKIPPED);
    }

}
