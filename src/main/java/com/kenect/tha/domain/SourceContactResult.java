package com.kenect.tha.domain;

import java.util.List;

import com.kenect.tha.model.SourceContact;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// class to represent source info
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceContactResult {

  private int currentPage;
  private int pageItems;
  private int totalPages;
  private int totalCount;

  private List<SourceContact> listSourceContact;

}
