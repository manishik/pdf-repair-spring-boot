package com.example.pdfrepair.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class RepairRequest {

    @JsonProperty("File")
    @JsonAlias("file")
    MultipartFile file;
}
