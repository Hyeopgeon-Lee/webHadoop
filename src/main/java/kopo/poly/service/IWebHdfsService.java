package kopo.poly.service;

import kopo.poly.dto.WebHdfsDTO;

/**
 * WebHDFS operations used by the controller.
 * Connection settings are externalized in application.properties.
 */
public interface IWebHdfsService {

    String upload(WebHdfsDTO request);

    String delete(WebHdfsDTO request);

    String list(WebHdfsDTO request);
}
