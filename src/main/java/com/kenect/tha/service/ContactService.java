package com.kenect.tha.service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kenect.tha.domain.SourceContactResult;
import com.kenect.tha.model.Contact;
import com.kenect.tha.model.SourceContact;
import com.kenect.tha.repository.SourceContactRepositoryInterface;

@Service
public class ContactService {

  // Function Interface to hold the request
  @Autowired
  private SourceContactRepositoryInterface sourceContactRepository;

  // Semaphore to limit the amount of simultaneous threads
  private static final Semaphore semaphore = new Semaphore(10);

  // method to acquire semaphore, that has to be wrapped in a try/catch block
  private static void acquire() {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public List<Contact> getAllContacts() throws Exception {

    // class used as a lock and as a comunication between lamdba function and its
    // parent method
    class MutableParameter {
      Integer pageNumber = 1;
      Integer totalPages = 1;
      String errorMessage = null;
    }

    // Variable to hold the result
    List<Contact> result = new ArrayList<>();

    // Instance of MutableParameter
    MutableParameter mutableParameter = new MutableParameter();

    // Executor to run simultaneous tasks
    ExecutorService executor = Executors.newCachedThreadPool();

    // functoin to fetch the page and add it into result
    Runnable appendToResult = () -> {

      // here we need to lock the execution until this method executes less than
      // 10 threads
      acquire();

      // variable to hold the list of SourceContacts from a page
      SourceContactResult sourceContactResult;

      try {

        // retrieve page info in SourceContactResult format
        sourceContactResult = sourceContactRepository.requestPage(mutableParameter.pageNumber);

        // Race condition prevetion and comunication through mutableParameter
        // fields
        synchronized (mutableParameter) {

          // Here we iterate through the items and add each one into the result
          for (SourceContact sourceContact : sourceContactResult.getListSourceContact()) {

            result.add(new Contact(sourceContact.getId(),
                sourceContact.getFirstName() + " " + sourceContact.getLastName(),
                sourceContact.getEmailAddress(),
                "KENECT_LABS",
                Instant.ofEpochMilli(sourceContact.getCreatedDate()),
                Instant.ofEpochMilli(sourceContact.getUpdatedDate())));

          }

        }

        // here we increment the page number for the next thread.
        mutableParameter.pageNumber++;

        // here we define the total pages (more than once, since this info is dynamic
        // and can change from request to request)
        mutableParameter.totalPages = sourceContactResult.getTotalPages();

        // releases the semaphore to allow one new thread to enter the 10 pool sized
        // cached
        semaphore.release();

      } catch (IOException e) {

        // if one request goes wrong, interrupt everything and attrib variable to be
        // used on an exception throw
        mutableParameter.errorMessage = e.getMessage();
        executor.shutdownNow();

        e.printStackTrace();
      }

    };

    // fetch the page for the first time to get total pages
    appendToResult.run();

    // iterates through the rest of pages, adds the runnable into a cached
    // thread pool and executes them
    for (int i = 2; i <= mutableParameter.totalPages; i++) {

      executor.execute(appendToResult);

    }

    // gracefull shutdown
    executor.shutdown();

    // waits until every task is done
    executor.awaitTermination(100, TimeUnit.SECONDS);

    // throws exception in case of previous error
    if (mutableParameter.errorMessage != null) {

      throw new IOException(mutableParameter.errorMessage);

    }

    // returns the result
    return result;

  }

}
