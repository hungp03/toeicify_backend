package com.toeicify.toeic.util.enums;

import java.util.Arrays;
import java.util.Optional;

public enum ToeicPartSpec {
    P1(1, 6), P2(2, 25), P3(3, 39), P4(4, 30), P5(5, 30), P6(6, 16), P7(7, 54);

    public final int partNumber;
    public final int expectedQuestionCount;

    ToeicPartSpec(int partNumber, int expected) {
        this.partNumber = partNumber;
        this.expectedQuestionCount = expected;
    }

    public static int expectedFor(int partNumber) {
        return find(partNumber).map(p -> p.expectedQuestionCount).orElse(0);
    }

    public static Optional<ToeicPartSpec> find(int partNumber) {
        return Arrays.stream(values()).filter(p -> p.partNumber == partNumber).findFirst();
    }

    public static boolean isValid(int partNumber) {
        return find(partNumber).isPresent();
    }
}
