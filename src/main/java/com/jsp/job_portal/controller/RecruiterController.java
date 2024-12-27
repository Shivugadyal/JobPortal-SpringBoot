
package com.jsp.job_portal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jsp.job_portal.dto.Job;
import com.jsp.job_portal.dto.Recruiter;
import com.jsp.job_portal.repository.JobRepository;
import com.jsp.job_portal.service.RecruiterService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

	@Autowired
	RecruiterService recruiterService;

	@Autowired
	JobRepository jobRepository;

	@GetMapping("/register")
	public String loadRegister(Recruiter recruiter, ModelMap map) {
		map.put("recruiter", recruiter);
		return "recruiter-register.html";
	}

	@PostMapping("/register")
	public String register(@Valid Recruiter recruiter, BindingResult result, HttpSession session) {
		return recruiterService.register(recruiter, result, session);
	}

	@GetMapping("/otp/{id}")
	public String loadOtp(ModelMap map, @PathVariable("id") int id) {
		map.put("id", id);
		return "recruiter-otp.html";
	}

	@PostMapping("/otp")
	public String otp(@RequestParam("id") int id, @RequestParam("otp") int otp, HttpSession session) {
		return recruiterService.otp(id, otp, session);
	}

	@GetMapping("/resend-otp/{id}")
	public String resendOtp(@PathVariable("id") int id, HttpSession session) {
		return recruiterService.resendOtp(id, session);
	}

	@GetMapping("/home")
	public String loadHome(HttpSession session) {
		if (session.getAttribute("recruiter") != null) {
			return "recruiter-home.html";
		} else {
			session.setAttribute("error", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/post-job")
	public String loadPostJob(HttpSession session) {
		if (session.getAttribute("recruiter") != null) {
			return "post-job.html";
		} else {
			session.setAttribute("error", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@PostMapping("/post-job")
	public String postJob(Job job, HttpSession session) {
		if (session.getAttribute("recruiter") != null) {
			Recruiter recruiter = (Recruiter) session.getAttribute("recruiter");
			job.setRecruiter(recruiter);
			jobRepository.save(job);
			session.setAttribute("success", "Job Posted Success");
			return "redirect:/recruiter/home";
		} else {
			session.setAttribute("error", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}
	@GetMapping("/manage-job")
	public String manageJobs(HttpSession session, ModelMap map) {
	    if (session.getAttribute("recruiter") != null) {
	        Recruiter recruiter = (Recruiter) session.getAttribute("recruiter");
	        List<Job> jobs = jobRepository.findByRecruiter(recruiter);

	        if (jobs.isEmpty()) {
	            map.put("message", "No Jobs Found");
	        } else {
	            map.put("jobs", jobs);
	        }

	        return "manage-jobs.html";
	    } else {
	        session.setAttribute("error", "Invalid Session, Login Again");
	        return "redirect:/login";
	    }
	}


	@GetMapping("/edit-job")
	public String editJob(@RequestParam int id, HttpSession session, ModelMap map) {
	    if (session.getAttribute("recruiter") != null) {
	        Optional<Job> jobOptional = jobRepository.findById(id);
	        if (jobOptional.isPresent()) {
	            map.put("job", jobOptional.get());
	            return "edit-job.html";
	        } else {
	            session.setAttribute("error", "Job Not Found");
	            return "redirect:/recruiter/manage-job";
	        }
	    } else {
	        session.setAttribute("error", "Invalid Session, Login Again");
	        return "redirect:/login";
	    }
	}
	
	@GetMapping("/delete-job")
	public String deleteJob(@RequestParam int id, HttpSession session) {
	    if (session.getAttribute("recruiter") != null) {
	        jobRepository.deleteById(id);
	        session.setAttribute("success", "Job Deleted Successfully");
	        return "redirect:/recruiter/manage-job";
	    } else {
	        session.setAttribute("error", "Invalid Session, Login Again");
	        return "redirect:/login";
	    }
	}

	@PostMapping("/update-job")
	public String updateJob(@ModelAttribute Job job, HttpSession session) {
	    if (session.getAttribute("recruiter") != null) {
	    	Recruiter recruiter = (Recruiter) session.getAttribute("recruiter");
			job.setRecruiter(recruiter);
	        jobRepository.save(job); // Save the updated job
	        session.setAttribute("success", "Job Updated Successfully");
	        return "redirect:/recruiter/manage-job"; // Redirect to manage jobs
	    } else {
	        session.setAttribute("error", "Invalid Session, Login Again");
	        return "redirect:/login";
	    }
	}

	
	@GetMapping("/status/{id}")
	public String changeStatus(@PathVariable("id") int id,HttpSession session) {
		if (session.getAttribute("Admin") != null) {
			Job job=jobRepository.findById(id).orElseThrow();
			if(job.isApproved())
				job.setApproved(false);
			else
				job.setApproved(true);
		
			jobRepository.save(job);
			session.setAttribute("success", "Status Changed Success");
			return "redirect:/admin";
		}else {
			session.setAttribute("error", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}


	}
	
