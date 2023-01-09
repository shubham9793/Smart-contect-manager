package com.smart.contact.Dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.contact.Entities.Contact;
import com.smart.contact.Entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	//pegination method...
	@Query("from Contact as c where c.user.id =:userId")
	 
	
	//CurrentPage-page
	//Contact Per Page-5
	public Page<Contact> findContactsByUser(@Param("userId")int userId,Pageable pePageable);
	
	//Search Query 
	//@Query("")
	public List<Contact> findByNameContainingAndUser(String name,User user);
}
