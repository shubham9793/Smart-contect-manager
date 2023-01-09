package com.smart.contact.Controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.contact.Dao.UserRepository;
import com.smart.contact.Entities.User;
import com.smart.contact.helper.Message_;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {

		model.addAttribute("title", "Home- Smart Contact Manager");
		return "home";

	}

	@RequestMapping("/about")
	public String about(Model model) {

		model.addAttribute("title", "About- Smart Contact Manager");
		return "about";

	}

	@RequestMapping("/signup")
	public String signup(Model model) {

		model.addAttribute("title", "Signup- Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";

	}

	// handler for registering user
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("you have not agreed the terms and conditions");
				throw new Exception("you have not agreed the terms and conditions");
			}

			if (result1.hasErrors()) {
				System.out.println("Error" + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			System.out.println("agreement " + agreement);
			System.out.println("User " + user);

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageurl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			User result = this.userRepository.save(user);
			model.addAttribute("user ", new User());
			session.setAttribute("message", new Message_("Successfully Registered !", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user " + user);

			session.setAttribute("message", new Message_("Something went wrong ! " + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

	
	// handler for Custome login
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title","Login Page");
		return "login";
	}
}
