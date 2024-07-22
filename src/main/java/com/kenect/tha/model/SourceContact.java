package com.kenect.tha.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// class to represent input format
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceContact {

  private int id;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private Long createdDate;
  private Long updatedDate;

}
