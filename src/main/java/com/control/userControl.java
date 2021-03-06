package com.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fenye.Fenye;
import com.service.BlogService;
import com.service.Service;
import com.service.UserService;
import com.user.User;


@Controller
@RequestMapping("/user")
public class userControl extends control {
	
	@Autowired
	private Service service;
	
	@Autowired
	private UserService userservice;
	
	@Autowired
	private BlogService cardservice;
	
	@RequestMapping("/back")
	public String back(HttpSession session) {
		session.invalidate();
		return "login";
	}//这里之所以要加一个退出方法是为了使会话失效,不然推出根据url地址依旧可以跳过登录步骤进入
	
	@RequestMapping("/load")
	public String load( String name, MultipartFile file,HttpSession session,HttpServletRequest req, HttpServletResponse resp) throws Exception, IOException { 
			if (!file.isEmpty()) {
		            byte[] bytes = file.getBytes();
		        	String src = req.getServletContext().getRealPath("/");
		    		
		    		String path=null;
		    		
		    		if(src.endsWith(File.separator)) {
		    			path="WEB-INF";
		    		}
		    		else {
		    			path="/WEB-INF";
		    		}
		    	 User user = (User)session.getAttribute("user");
			     String fileName = "" + user.getUsername();
		    	 File loadedFile = new File(src +path+"/load/" +  fileName);//这里创建文件保存的是上传文件的实际存放地址
		    	 file.transferTo(loadedFile);//transferTo(上传文件的存放路径)
		    	 return toUsercenter(req, resp);
			}
			return toUsercenter(req, resp);
	}//这里的上传程序格式固定只需要根据要求创建不同的文件存在地址就可以了,其它的都只需要按照文档中的web部分就可以
	
	@RequestMapping("/toUpload")
	public String toUpload(HttpSession session) {
		return "load";
	}

	
	 @RequestMapping("/showPicture")
	public void showPicture(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String src=req.getServletContext().getRealPath("/");
		System.out.println(">>>>>>>"+src);
		HttpSession session=req.getSession();//这里的一个知识点就是session和request的区别一个是全局变量一个是局部变量
		User user=(User)session.getAttribute("user");

		String path=null;
		
		if(src.endsWith(File.separator)) {
			path="WEB-INF";
		}
		else {
			path="/WEB-INF";
		}
		try (FileInputStream in = new FileInputStream(src+path+"/load/" + user.getUsername());
				OutputStream out = resp.getOutputStream()) {
			byte[] data = new byte[1024];
			int length = -1;
			while((length=in.read(data))!=-1) {
				out.write(data, 0, length);
				out.flush();
			}
		}
	}
	
	@RequestMapping("/toRegister")
	public String  toRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();
		return "register";
	
	}
	
	@RequestMapping("/Register")
	public String Register(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();//如果有进行赋值就是原来的Session，如果没有创建新的Session
		String username= req.getParameter("username");
		String psssword= req.getParameter("password");
		User user= userservice.register(username,psssword);
		if(user==null) {
			return "register";
		}
		else {
			session.setAttribute("user",user);
			return "login";
		}
	}
	
	
	@RequestMapping("/toLogin")
	public String toLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();
		return "login";																																								
	}
	
	@RequestMapping("/login")
	public String  login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		javax.servlet.http.HttpSession session = req.getSession();
		String username= req.getParameter("username");
		String psssword= req.getParameter("password");
		User user=userservice.login(username, psssword);
		if(null==user) {
			return "login";	
		}
		else {
			session.setAttribute("user", user);
			session.setAttribute("username",username);
			return toUsercenter(req, resp);
		}
	}
	
	@RequestMapping("/toUsercenter")
	public String toUsercenter(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("--------");
		javax.servlet.http.HttpSession session = req.getSession();
		String username= (String) session.getAttribute("username");
		System.out.println(username);
		String currentPage = req.getParameter("currentPage");
		currentPage=currentPage ==null ? "1" : currentPage;//注意这里的分页后台实现
		Fenye list=cardservice.list(username, Integer.parseInt(currentPage));
		System.out.println("\\\\\\"+list);
		req.setAttribute("fenye", list);
		req.setAttribute("currentPage", Integer.parseInt(currentPage));
		req.setAttribute("username", username);
		return "usercenter";
	}
	

}
