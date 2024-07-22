package com.kenect.tha.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kenect.tha.domain.SourceContactResult;
import com.kenect.tha.model.SourceContact;

// Implementation of function interface used to obtain information about the contacts
@Component
public class SourceContactRestRequest implements SourceContactRepositoryInterface {

  // URL Address
  private static String url = "http://154.53.38.236:8086/wk/sniper/contacts";

  // function to transform connection buffer into a string
  private static String BufferToString(HttpURLConnection con) throws IOException {

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();

    return response.toString();

  }

  // function to transform connection buffer into a lista of SourceContact
  private static List<SourceContact> BufferToListSourceContact(HttpURLConnection con) throws IOException {

    ObjectMapper objectMapper = new ObjectMapper();

    List<SourceContact> listSourceContact = objectMapper.readValue(BufferToString(con),
        new TypeReference<List<SourceContact>>() {
        });

    return listSourceContact;

  }

  // method do retrive info of a given page and return a SourceContactResult
  public SourceContactResult requestPage(Integer page) throws IOException {

    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("GET");
    int responseCode = con.getResponseCode();

    if (responseCode != 200) {
      throw new IOException("Error reading source (" + responseCode + "-" + con.getResponseMessage() + ")");
    }

    Map<String, List<String>> headers = con.getHeaderFields();
    Integer currentPage = Integer.valueOf(headers.get("Current-Page").get(0));
    Integer pageItems = Integer.valueOf(headers.get("Page-Items").get(0));
    Integer totalPages = Integer.valueOf(headers.get("Total-Pages").get(0));
    Integer totalCount = Integer.valueOf(headers.get("Total-Count").get(0));

    List<SourceContact> listSourceContact = BufferToListSourceContact(con);

    return new SourceContactResult(currentPage,
        pageItems,
        totalPages,
        totalCount,
        listSourceContact);

  }

}
