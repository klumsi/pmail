package im.bzh.controller;

import im.bzh.dto.MailDTO;
import im.bzh.dto.MailTransferDTO;
import im.bzh.entity.Envelope;
import im.bzh.entity.Mail;
import im.bzh.service.MailService;
import im.bzh.utils.R;
import im.bzh.vo.DraftVO;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @GetMapping("/{username}/{folder}")
    public R getEnvelopeList(@PathVariable String username, @PathVariable String folder) throws Exception {
        try {
            List<Envelope> envelopeList = mailService.getEnvelopeList(username, folder);
            return new R(true, null, envelopeList);
        } catch (Exception e) {
            e.printStackTrace();
            return new R(false, "failed to get envelope list", null);
        }
    }

    @PutMapping("/{username}/{folder}")
    public R updateMail(@PathVariable String username, @PathVariable String folder, @RequestBody MailTransferDTO mailTransferDTO) throws Exception {
        if (mailTransferDTO.getType().equals("MARK_AS_READ")) {
            boolean success = mailService.markMailAsRead(username, folder, mailTransferDTO.getIds());
            return new R(success, success ? null : "failed to update", null);
        }
        if (mailTransferDTO.getType().equals("MARK_AS_UNREAD")) {
            boolean success = mailService.markMailAsUnread(username, folder, mailTransferDTO.getIds());
            return new R(success, success ? null : "failed to update", null);
        }
        if (mailTransferDTO.getType().equals("MOVE")) {
            boolean success = mailService.moveMail(username, folder, mailTransferDTO.getIds(), mailTransferDTO.getDestination());
            return new R(success, success ? null : "failed to update", null);
        }
        return new R(false, "invalid parameter", null);
    }

    @DeleteMapping("/{username}/{folder}")
    public R deleteMail(@PathVariable String username, @PathVariable String folder, @RequestBody MailTransferDTO mailTransferDTO) throws Exception {
        boolean success = mailService.deleteMail(username, folder, mailTransferDTO.getIds(), mailTransferDTO.getDeletePermanently());
        return new R(success, success ? null : "failed to delete", null);
    }

    @GetMapping("/{username}/{folder}/{id}")
    public R getMail(@PathVariable String username, @PathVariable String folder, @PathVariable Long id) throws Exception {
        if (!folder.equals("drafts")) {
            try {
                Mail mail = mailService.getMail(username, folder, id);
                return new R(true, null, mail);
            } catch (Exception e) {
                return new R(false, "mail does not exist", null);
            }
        }
        DraftVO draftVO = mailService.getDraftVO(username, folder, id);
        return new R(draftVO != null, draftVO != null ? null : "mail does not exist", draftVO);
    }

    @GetMapping("/{username}/{folder}/{id}/{fileName}")
    public void getAttachment(@PathVariable String username, @PathVariable String folder, @PathVariable Long id, @PathVariable String fileName, HttpServletResponse response) throws Exception {
        InputStream in = mailService.getAttachmentInputStream(username, folder, id, fileName);
        IOUtils.copy(in, response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping
    public R sendMail(@RequestBody MailDTO mailDTO) {
        boolean success = mailService.sendMail(mailDTO);
        return new R(success, success ? null : "failed to send", null);
    }

    @PostMapping("/save")
    public R saveToDrafts(@RequestBody MailDTO mailDTO) throws Exception {
        boolean success = mailService.saveMailToDrafts(mailDTO);
        return new R(success, success ? null : "failed to save", null);
    }

}

