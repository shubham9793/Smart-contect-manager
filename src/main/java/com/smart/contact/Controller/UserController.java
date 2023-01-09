package com.smart.contact.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.apache.catalina.webresources.FileResourceSet;
import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.contact.Dao.ContactRepository;
import com.smart.contact.Dao.UserRepository;
import com.smart.contact.Entities.Contact;
import com.smart.contact.Entities.User;
import com.smart.contact.helper.Message_;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository  contactRepository;
	
	
	// method for adding common data to response
	@ModelAttribute
	public void addCommonData( Model model,Principal principal) {
		
		String username = principal.getName();
		System.out.println("Username  "+username);
		
		// get the user using  username("email")
		User user = userRepository.getUserByUserName(username);
		System.out.println("user "+user);
		model.addAttribute("user",user);
	}
	
	
	
	//Dashboard Home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	// processing add contact form
	//process-contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		
		try {
			
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file 
			if(file.isEmpty()) {
				System.out.println("file is empty");
				contact.setImage("contact.png");
			}else {
				//upload the file in folder
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Successfully Uploaded");	
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);		
			System.out.println("Data" + contact);
			System.out.println("Data added in Db successfully");
			
			//messgae success
			session.setAttribute("message", new Message_("Your Contact is added ! add more...", "success"));
			
			
		}catch (Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			
			//Error msg
			session.setAttribute("message", new Message_("Something went Wrong !", "danger"));
			
		}
		
		return "normal/add_contact_form";
	}
	
	
	//Show Contact Mehtod
	//parpage  = 5[n]
	//current Page = 0[]
	
	@RequestMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title" ,"Show User Contacts");
		
		// send all contacts list
		
//		List<Contact> contacts = user.getContacts();
		
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		//CurrentPage-page
		//Contact Per Page-5
		Pageable pageable = PageRequest.of(page,8);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model,Principal principal) {
		
		System.out.println("Cid " + cId);
		Optional<Contact> contactoptional = this.contactRepository.findById(cId);
		
		Contact contact = contactoptional.get();
		
		//
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact" , contact);
			model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	
	//delete Contact handller
	
	@GetMapping("/delete/{cId}")
	public String DeleteContact(@PathVariable("cId") Integer cId,Model model, Principal principal , HttpSession session) {
		
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message_("Contact deleted successfully","success"));
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	//open Update Form  handler
	
	@PostMapping("/open-contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid, Model model) {
		model.addAttribute("title","Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	
	// Update Contact handler
	@RequestMapping(value="/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage")MultipartFile file,
			HttpSession session,Principal principal) {
		
		Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
		
		try {
			if(!file.isEmpty()) {
				
				// rewrite
				//delete old photo
				File deletefile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deletefile,oldContactDetail.getImage());
				
				file1.delete();
				
				
				// update new Photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			} else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message",new Message_("Your Contact is update !" ,"success"));
			
		}catch (Exception e) {
			// TODO: handle exception
		}	
		
		System.out.println("Contact Name "  + contact.getName());
		System.out.println("Contact Id "+contact.getcId());
		
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	//Your profile Handler
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile Page");
		
		return "normal/profile";
	}
}
