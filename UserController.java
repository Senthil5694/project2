package com.Controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.Dao.UserDao;
import com.Model.UserDetails;

@Controller
public class UserController {

	@Autowired
	private UserDao userdao;

	@Autowired
	private UserDetails userdetails;

	// this method will return list of userdetails from the database
	@RequestMapping(value = "/listusers", method = RequestMethod.GET)
	public ResponseEntity<List<UserDetails>> list() {
		List<UserDetails> userlist = userdao.list();
		if (userlist.isEmpty()) {
			userdetails.setErrorcode("404");
			userdetails.setErrormessage("users not available");

			userlist.add(userdetails);
		}
		return new ResponseEntity<List<UserDetails>>(userlist, HttpStatus.OK);

	}
	// this method will check whether the given userid exists or not
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public ResponseEntity<UserDetails> get(@PathVariable(value = "userid") String userid) {
		userdetails = userdao.getuser(userid);

		if (userdetails == null) {
			userdetails = new UserDetails();
			userdetails.setErrorcode("404");
			userdetails.setErrormessage("user is not exists with this id"+userdetails.getUserid());
		}
		return new ResponseEntity<UserDetails>(userdetails, HttpStatus.OK);

	}

	// this method will validate whether the entered userid and password are
	// correct or not
	@RequestMapping(value = "/validateuser", method = RequestMethod.GET)
	public ResponseEntity<UserDetails> login(@RequestBody UserDetails userdetails, HttpSession httpSession) {
		userdao.authenticate(userdetails.getUserid(),userdetails.getPassword());
		userdetails=userdao.authenticate(userdetails.getUserid(),userdetails.getPassword());

		if (userdetails == null) {
			userdetails = new UserDetails();
			userdetails.setErrorcode("404");
			userdetails.setErrormessage("Invalid credentials please try again");

		} else {
			if(userdetails.getStatus()=="R")
			{
				userdetails.setErrorcode("404");
				userdetails.setErrormessage("You are not approved to login please contact admin");
			}
			else{
				httpSession.setAttribute("loggedinuserid", userdetails.getUserid());
				userdetails.setIsonline("Y");
				userdao.save(userdetails);
			}
			
		}
		return new ResponseEntity<UserDetails>(userdetails, HttpStatus.OK);
	}
//this method is used to logout from the session
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public ResponseEntity<UserDetails> logout(HttpSession httpSession) {
		// remove the attribute from http session
		// update the status as offline
		String loggedinuserid = (String) httpSession.getAttribute("loggedinuserid");
		userdao.getuser(loggedinuserid);
		userdetails.setIsonline("N");

		if (userdao.update(userdetails)) {
			userdetails.setErrorcode("200");
			userdetails.setErrormessage("Logout successfull");
		} else {
			userdetails.setErrorcode("404");
			userdetails.setErrormessage("problem occured while logging out");
		}
		httpSession.invalidate();
		return new ResponseEntity<UserDetails>(userdetails, HttpStatus.OK);

	}
	//this method is used to accept a user by using his userid
	@RequestMapping(value="/accept/{userid}",method=RequestMethod.POST)
	public ResponseEntity<UserDetails> acceptuser(@PathVariable(value="userid")String userid,HttpSession httpSession) {
     userdetails=userdao.getuser(userid);
      userdetails.setStatus("A");
      userdao.update(userdetails);
      return new ResponseEntity<UserDetails>(userdetails,HttpStatus.OK);

	}
	//this method is used to save the userdetails to the database when the user registers
	@RequestMapping(value="/register",method=RequestMethod.POST)
	public ResponseEntity<UserDetails> registeruser(@RequestBody UserDetails userdetails,HttpSession httpSession) {
		if(userdao.getuser(userdetails.getUserid())==null)
		{
			userdetails.setStatus("R");
			userdetails.setIsonline("N");
			userdao.save(userdetails);
			userdetails.setErrormessage("You have Registered successfully please login to continue");
			return new ResponseEntity<UserDetails>(userdetails,HttpStatus.OK);
		}else
		{
			userdetails.setErrorcode("404");
			userdetails.setErrormessage("User id already exists please try with new userid :"+ userdetails.getUserid());
			return new ResponseEntity<UserDetails>(userdetails,HttpStatus.OK);
		}

	}

}
