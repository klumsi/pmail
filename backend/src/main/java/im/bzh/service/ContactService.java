package im.bzh.service;

import im.bzh.entity.Contact;

import java.util.List;

public interface ContactService {
    List<Contact> getListByCreator(String creator);

    boolean addContact(Contact contact);

    Contact getContactByAddressAndCreator(String address, String creator);

    boolean deleteContact(Long[] ids);
}
