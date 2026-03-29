package com.jemini.stayhost.property.domain.component;

import com.jemini.stayhost.property.domain.model.Inventory;

import java.util.List;

public interface InventoryManager {

  List<Inventory> saveAll(List<Inventory> inventories);
}
