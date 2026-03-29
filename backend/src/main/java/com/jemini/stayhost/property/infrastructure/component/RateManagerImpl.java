package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.property.domain.component.RateManager;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.infrastructure.persistence.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RateManagerImpl implements RateManager {

  private final RateRepository rateRepository;

  @Override
  public List<Rate> saveAll(final List<Rate> rates) {
    return rateRepository.saveAll(rates);
  }
}
