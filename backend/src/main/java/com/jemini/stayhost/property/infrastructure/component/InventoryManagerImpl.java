package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.property.domain.component.InventoryManager;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.infrastructure.persistence.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryManagerImpl implements InventoryManager {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<Inventory> saveAll(final List<Inventory> inventories) {
        return inventoryRepository.saveAll(inventories);
    }
}
