package com.jemini.stayhost.property.domain.component;

import com.jemini.stayhost.property.domain.model.Rate;

import java.util.List;

public interface RateManager {

    List<Rate> saveAll(List<Rate> rates);
}
