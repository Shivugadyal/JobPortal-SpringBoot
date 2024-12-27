package com.jsp.job_portal.controller;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.job_portal.dto.Admin;
import com.jsp.job_portal.dto.Job;
import com.jsp.job_portal.dto.JobSeeker;
import com.jsp.job_portal.dto.Recruiter;
import com.jsp.job_portal.helper.AES;
import com.jsp.job_portal.repository.JobRepository;
import com.jsp.job_portal.repository.JobSeekerRepository;
import com.jsp.job_portal.repository.RecruiterRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class GeneralController {

	@Autowired
	JobSeekerRepository jobSeekerRepository;

	@Autowired
	RecruiterRepository recruiterRepository;
	
	@Autowired
	JobRepository jobRepository;
	
	


	@GetMapping("/")
	public String loadHome() {
		return "home.html";
	}

	@GetMapping("/about-us")
	public String loadAbout() {
		return "about-us.html";
	}

	@GetMapping("/contact")
	public String contact() {
		return "contact.html";
	}

	@GetMapping("/privacy-policy")
	public String services() {
		return "privacy-policy.html";
	}

	@GetMapping("/terms")
	public String loadTerms() {
		return "terms.html";
	}

	@GetMapping("/login")
	public String loadLogin() {
		return "login.html";
	}
	
	
	@GetMapping("/adminhome")
	public String loadAdminHome(HttpSession session) {
		if (session.getAttribute("Admin") != null) {
			session.setAttribute("success", "Logged in Successfully");
			return "adminhome.html";
		} else {
			session.setAttribute("error", "Invalid Session, Login Again");
			return "redirect:/login";
		}
		
		
		
	}
	@GetMapping("/logoutadmin")
	public String logoutAdmin(HttpSession session) {
		session.removeAttribute("Admin");
		session.setAttribute("error", "Logged Out Successfully");
		return "redirect:/login";
	}
	
	
	@PostMapping("/login")
	public String login(@RequestParam("emph") String emph, @RequestParam("password") String password,
			HttpSession session,Admin main) {
		
		main.setUsername("admin");
		main.setPassword("admin");
		
		if(emph.equals("admin")&&password.equals("admin")){
		
			session.setAttribute("Admin", main);
//				session.setAttribute("success", "Logged in Successfully");
				return "redirect:/adminhome";
				}
			
//		else {
//			session.setAttribute("error", "Invalid data");
//			return "redirect:/login";}
			
		else {
		Long mobile = null;
		String email = null;

		try {
			mobile = Long.parseLong(emph);
			Recruiter recruiter = recruiterRepository.findByMobile(mobile);
			JobSeeker jobSeeker = jobSeekerRepository.findByMobile(mobile);
			if (recruiter == null && jobSeeker == null) {
				session.setAttribute("error", "Invalid Mobile Number");
				return "redirect:/login";
			} else {
				if (recruiter != null) {
					if (AES.decrypt(recruiter.getPassword()).equals(password)) {
						session.setAttribute("success", "Login Success as Recruiter");
						session.setAttribute("recruiter", recruiter);
						return "redirect:/recruiter/home";
					} else {
						session.setAttribute("error", "Invalid Password");
						return "redirect:/login";
					}
				} else {
					if (AES.decrypt(jobSeeker.getPassword()).equals(password)) {
						session.setAttribute("success", "Login Success as JobSeeker");
						session.setAttribute("jobSeeker", jobSeeker);
						return "redirect:/jobseeker/home";
					} else {
						session.setAttribute("error", "Invalid Password");
						return "redirect:/login";
					}
				}
			}

		} catch (NumberFormatException e) {
			email = emph;
			Recruiter recruiter = recruiterRepository.findByEmail(email);
			JobSeeker jobSeeker = jobSeekerRepository.findByEmail(email);
			if (recruiter == null && jobSeeker == null) {
				session.setAttribute("error", "Invalid Email");
				return "redirect:/login";
			} else {
				if (recruiter != null) {
					if (AES.decrypt(recruiter.getPassword()).equals(password)) {
						session.setAttribute("success", "Login Success as Recruiter");
						session.setAttribute("recruiter", recruiter);
						return "redirect:/recruiter/home";
					} else {
						session.setAttribute("error", "Invalid Password");
						return "redirect:/login";
					}
				} else {
					if (AES.decrypt(jobSeeker.getPassword()).equals(password)) {
						session.setAttribute("success", "Login Success as JobSeeker");
						session.setAttribute("jobSeeker", jobSeeker);
						return "redirect:/jobseeker/home";
					} else {
						session.setAttribute("error", "Invalid Password");
						return "redirect:/login";
					}
				}
			}

		}
		}
	}
	
	
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("jobSeeker");
		session.removeAttribute("recruiter");
		session.removeAttribute("Admin");
		session.setAttribute("error", "Logged Out Successfully");
		return "redirect:/";
	}
	

	
	@GetMapping("/admin")
	public String fetchJobs(ModelMap map,HttpSession session) {
		
		Recruiter recruiter = (Recruiter) session.getAttribute("recruiter");
		
	    List<Job> jobs = jobRepository.findAll(); // Fetch all jobs

	    
	    

	    if (jobs.isEmpty()) {
	        map.put("message", "No Jobs Found");
	    } else {
	    	
	        map.put("jobs", jobs);
	    }
	    if (session.getAttribute("Admin") != null) 
	    return "admin.html"; // No need for .html; Thymeleaf resolves automatically
	    
	    else {
			session.setAttribute("error", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}
	
	@PostMapping("/search-jobs")
	public String viewJobs(@RequestParam("query") String query,HttpSession session,ModelMap map) {
		List<Job> roleJobs=jobRepository.findByRoleLike("%"+query+"%");
		List<Job> skillJobs=jobRepository.findByRoleLike("%"+query+"%");
		HashSet<Job> jobs=new HashSet<Job>();
		jobs.addAll(skillJobs);
		jobs.addAll(roleJobs);
		
		if(jobs.isEmpty()) {
			session.setAttribute("error", "No Jobs Added Yet");
			return "redirect:/";
		}else {
			map.put("jobs", jobs);
			return "search-jobs-result.html";
		}
		
	}
}