package com.toeicify.toeic.projection;

import java.time.LocalDate;

/**
 * Created by hungpham on 8/10/2025
 */
public interface PracticeScorePoint {
    LocalDate getDay();
    Integer getScore();
}
