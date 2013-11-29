package ru.yandex.qatools.allure.storages;

import ru.yandex.qatools.allure.model.TestStepResult;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 06.11.13
 */
public final class TestStepStorage {

    private static final Map<Thread, Deque<TestStepResult>> testStepData = new ConcurrentHashMap<>();

    private TestStepStorage() {
    }

    private static void checkStep() {
        Thread currentThread = Thread.currentThread();
        if (!testStepData.containsKey(currentThread)) {
            testStepData.put(currentThread, new LinkedList<TestStepResult>());
        }

        if (testStepData.get(currentThread).isEmpty()) {
            testStepData.get(currentThread).add(new TestStepResult());
        }
    }

    public static TestStepResult getTestStep() {
        checkStep();
        return testStepData.get(Thread.currentThread()).getLast();
    }

    public static void putTestStep(TestStepResult testStepResult) {
        checkStep();
        testStepData.get(Thread.currentThread()).add(testStepResult);
    }

    public static TestStepResult pollTestStep() {
        checkStep();
        return testStepData.get(Thread.currentThread()).pollLast();
    }
}