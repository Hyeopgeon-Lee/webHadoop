package kopo.poly.controller;

import kopo.poly.dto.WebHdfsDTO;
import kopo.poly.service.IWebHdfsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WebHdfsControllerTest {
    @Test
    void uploadUsesConfiguredDirectory() throws Exception {
        IWebHdfsService service = mock(IWebHdfsService.class);
        when(service.upload(any(WebHdfsDTO.class))).thenReturn("true");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new WebHdfsController(service, "/01")).build();
        mvc.perform(post("/webhdfs/v1/upload")
                        .param("path", "subdir/file.txt").param("content", "hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File uploaded successfully"));
        ArgumentCaptor<WebHdfsDTO> captor = ArgumentCaptor.forClass(WebHdfsDTO.class);
        verify(service).upload(captor.capture());
        assertEquals("/01/subdir/file.txt", captor.getValue().getPath());
    }

    @Test
    void traversalReturns400() throws Exception {
        IWebHdfsService service = mock(IWebHdfsService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new WebHdfsController(service, "/01")).build();
        mvc.perform(post("/webhdfs/v1/upload")
                        .param("path", "../secrets").param("content", "test"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void deleteUsesQueryParameter() throws Exception {
        IWebHdfsService service = mock(IWebHdfsService.class);
        when(service.delete(any(WebHdfsDTO.class))).thenReturn("true");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new WebHdfsController(service, "/01")).build();
        mvc.perform(delete("/webhdfs/v1/delete").param("path", "file.txt"))
                .andExpect(status().isOk());
        ArgumentCaptor<WebHdfsDTO> captor = ArgumentCaptor.forClass(WebHdfsDTO.class);
        verify(service).delete(captor.capture());
        assertEquals("/01/file.txt", captor.getValue().getPath());
    }

    @Test
    void upstreamErrorsReturn502WithoutInternalDetails() throws Exception {
        IWebHdfsService service = mock(IWebHdfsService.class);
        when(service.list(any(WebHdfsDTO.class)))
                .thenThrow(new IllegalStateException("secret-server-url"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new WebHdfsController(service, "/01")).build();
        mvc.perform(get("/webhdfs/v1/list"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value(
                        "Failed to list directory (check WebHDFS connection and permissions)"));
    }
}
