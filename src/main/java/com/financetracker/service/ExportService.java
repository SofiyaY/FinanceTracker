package com.financetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financetracker.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExportService {
    private final ObjectMapper mapper;

    public ExportService() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void exportToJson(List<Transaction> transactions, File file) throws IOException {
        mapper.writeValue(file, transactions);
    }
}
