package com.kenect.tha.repository;

import java.io.IOException;

import com.kenect.tha.domain.SourceContactResult;

// functional interface to hold implementation of Source Contact Info
public interface SourceContactRepositoryInterface {

  SourceContactResult requestPage(Integer page) throws IOException;

}
