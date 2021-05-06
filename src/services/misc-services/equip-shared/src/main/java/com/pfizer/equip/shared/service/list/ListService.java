package com.pfizer.equip.shared.service.list;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pfizer.equip.shared.relational.entity.ListName;
import com.pfizer.equip.shared.relational.entity.ListValue;
import com.pfizer.equip.shared.relational.repository.ListNameRepository;

@Service
public class ListService {
   @Autowired
   private ListNameRepository listNameRepository;

   public List<ListValue> getList(String name) {
      ListName listName = listNameRepository.findByName(name);
      if (listName == null) {
         throw new ListNotFoundException("List with the name " + name + " could not be found");
      } else {
         return listName.getValues();
      }
   }

   public List<ListName> getAllLists() {
      List<ListName> results = new ArrayList<>();
      listNameRepository.findAll().forEach(results::add);
      return results;
   }
}
