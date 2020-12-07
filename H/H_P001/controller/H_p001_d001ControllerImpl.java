package project.yoonju.H.H_P001.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import project.sungho.security.member.CustomUser;
import project.yoonju.H.H_P001.service.H_p001_d001Service;
import project.yoonju.H.H_P001.vo.H_p001_d001VO;
import project.yoonju.H.H_P001.vo.ImageVO;




@Controller("boardController")
public class H_p001_d001ControllerImpl implements H_p001_d001Controller {
	private static final String ARTICLE_IMAGE_REPO = "C:\\board\\article_image";
	@Autowired
	H_p001_d001Service boardService;
	

	@Override
	@RequestMapping(value = "H/H_P001/listArticles.page", method = { RequestMethod.GET, RequestMethod.POST })				//글목록보기
	public ModelAndView listArticles(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewName = "/H_P001/listArticles.tiles";
		List articlesList = boardService.listArticles();
		System.out.println("Ctrl===============>>" + articlesList.size());
		ModelAndView mav = new ModelAndView(viewName);
		mav.addObject("articlesList", articlesList);
		return mav;

	}

	@Override
	@RequestMapping(value = "H/H_P001/listPage.page", method = {RequestMethod.GET, RequestMethod.POST})					//게시물목록 + 페이징추가
	public ModelAndView listPage(@RequestParam("num") int num, HttpServletRequest request, HttpServletResponse response) throws Exception {

		int count = boardService.count();	//전체 게시물 갯수					
		int postnum = 10 * num;	//한 페이지에서 출력할 게시물 갯수					
		int pagenumList = (int)Math.ceil((double)count/10);	//하단 페이징 번호	
		int displayPost = (num - 1) * 10;	// 출력할 게시물 					
		int pageNum_cnt = 10; //한번에 출력할 페이지 갯수							

		//표시되는 페이지 번호 중 마지막 번호 1차 계산		
		int endPageNum = (int)(Math.ceil(((double)num) / (double)pageNum_cnt)) * pageNum_cnt; 
		
		//표시되는 페이지 번호 중 첫번째 번호
		int startPageNum = endPageNum - (pageNum_cnt-1); 	
		
		// 표시되는 페이지 번호 중 마지막 번호 2차 계산
		int endPageNum_tmp = (int)(Math.ceil((double)count / (double)pageNum_cnt));
		
		if(endPageNum > endPageNum_tmp) {	
			endPageNum = endPageNum_tmp;
		}
		
		boolean prev = startPageNum == 1 ? false:true; 
		boolean next = endPageNum * pageNum_cnt >= count ? false:true;

		
		String viewName = "/H_P001/listPage.tiles";
		List articlesList = boardService.listPage(displayPost, postnum);

		ModelAndView mav = new ModelAndView(viewName);
		mav.addObject("articlesList", articlesList);
		mav.addObject("pagenum", pagenumList);
		mav.addObject("count", count);
		mav.addObject("startPageNum", startPageNum);
		mav.addObject("endPageNum", endPageNum);
		mav.addObject("endPageNum_tmp", endPageNum_tmp);
		mav.addObject("prev", prev);
		mav.addObject("next", next);
		mav.addObject("select", num);
		return mav;
	}
	

	@Override
	@RequestMapping(value = "H/H_P001/addNewArticle.user", method = RequestMethod.POST)										//글쓰기
	@ResponseBody
	public ResponseEntity addNewArticle(MultipartHttpServletRequest multipartRequest, HttpServletResponse response)
			throws Exception {
		multipartRequest.setCharacterEncoding("utf-8");
		Map<String, Object> articleMap = new HashMap<String, Object>();
		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			String value = multipartRequest.getParameter(name);
			articleMap.put(name, value);
		}
		
		String imageFileName = upload(multipartRequest);
		HttpSession session = multipartRequest.getSession();				
		
		CustomUser cus = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();				

		articleMap.put("post_parent", 0);
		articleMap.put("user_id", cus.getUsername());
		articleMap.put("imageFileName", imageFileName);
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			String post_num = boardService.addNewArticle(articleMap);
			if (imageFileName != null && imageFileName.length() != 0) {
				File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
				File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + post_num);
				FileUtils.moveFileToDirectory(srcFile, destDir, true);
			}
			message = "<script>";
			message += " alert('새글을 추가했습니다.');";
			message += " location.href='" + multipartRequest.getContextPath() + "/H/H_P001/listArticles.page'; ";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		} catch (Exception e) {
			File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
			srcFile.delete();

			message = " <script>";
			message += " alert('오류가 발생했습니다. 다시 시도해 주세요');');";
			message += " location.href='" + multipartRequest.getContextPath() + "H//H_P001/articleForm.page'; ";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			e.printStackTrace();
		}
		return resEnt;
	}

	@Override
	@RequestMapping(value = "H/H_P001/addrplyArticle.user", method = RequestMethod.POST)										//글쓰기
	@ResponseBody
	public ResponseEntity addrplyArticle(MultipartHttpServletRequest multipartRequest, HttpServletResponse response)
			throws Exception {
		multipartRequest.setCharacterEncoding("utf-8");
		Map<String, Object> articleMap = new HashMap<String, Object>();
		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			String value = multipartRequest.getParameter(name);
			articleMap.put(name, value);
		}
		
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			System.out.println(articleMap.get(name));
		}
		
		
		String imageFileName = upload(multipartRequest);
		HttpSession session = multipartRequest.getSession();				
		
		CustomUser cus = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();				

		articleMap.put("user_id", cus.getUsername());
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			String post_num = boardService.addrplyArticle(articleMap);
			System.out.println("post_num에 담긴 것 -------------->" + post_num);
			message = "<script>";
			message += " alert('새글을 추가했습니다.');";
			message += " location.href='" + multipartRequest.getContextPath() + "/H/H_P001/listArticles.page'; ";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		} catch (Exception e) {
			File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
			srcFile.delete();

			message = " <script>";
			message += " alert('오류가 발생했습니다. 다시 시도해 주세요');');";
			message += " location.href='" + multipartRequest.getContextPath() + "H//H_P001/articleForm.page'; ";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			e.printStackTrace();
		}
		return resEnt;
	}
	
	

	@RequestMapping(value = "H/H_P001/viewArticle.page", method = RequestMethod.GET)									//한 개 글보기
	public ModelAndView viewArticle(@RequestParam("post_num") String post_num, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String viewName = "/H_P001/viewArticle.tiles";
		H_p001_d001VO articleVO = boardService.viewArticle(post_num);
		ModelAndView mav = new ModelAndView();
		mav.setViewName(viewName);
		mav.addObject("article2", articleVO);
		
		return mav;
	}

	@RequestMapping(value = "H/H_P001/modifyArticle.user", method = RequestMethod.POST)									//글 수정하기
	@ResponseBody
	public ResponseEntity modArticle(MultipartHttpServletRequest multipartRequest, HttpServletResponse response)
			throws Exception {
		multipartRequest.setCharacterEncoding("utf-8");
		Map<String, Object> articleMap = new HashMap<String, Object>();
		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			String value = multipartRequest.getParameter(name);
			articleMap.put(name, value);
		}

		String imageFileName = upload(multipartRequest);
		HttpSession session = multipartRequest.getSession();

		CustomUser cus = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
					
		articleMap.put("user_id", cus.getUsername());
		articleMap.put("imageFileName", imageFileName);
		
		String post_num = (String)articleMap.get("post_num");
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			boardService.modArticle(articleMap);
			if (imageFileName != null && imageFileName.length() != 0) { 
				File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
				File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + post_num);
				FileUtils.moveFileToDirectory(srcFile, destDir, true);

				String originalFileName = (String) articleMap.get("originalFileName");
				File oldFile = new File(ARTICLE_IMAGE_REPO + "\\" + post_num + "\\" + originalFileName);
				oldFile.delete();
			}
			message = "<script>";
			message += " alert('글을 수정했습니다.');";
			message += " location.href='" + multipartRequest.getContextPath() + "/H/H_P001/viewArticle.page?post_num="
					+ post_num + "';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		} catch (Exception e) {
			File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
			srcFile.delete();
			message = "<script>";
			message += " alert('오류가 발생했습니다.다시 수정해주세요');";
			message += " location.href='" + multipartRequest.getContextPath() + "/board/viewArticle.page?post_num="
					+ post_num + "';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		}
		return resEnt;
	}

	@Override
	@RequestMapping(value = "H/H_P001/removeArticle.user", method = RequestMethod.POST)										//글삭제하기
	@ResponseBody
	public ResponseEntity removeArticle(@RequestParam("post_num") String post_num, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setContentType("text/html; charset=UTF-8");
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			boardService.removeArticle(post_num);
			File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + post_num);
			FileUtils.deleteDirectory(destDir);

			message = "<script>";
			message += " alert('글을 삭제했습니다.');";
			message += " location.href='" + request.getContextPath() + "/H/H_P001/listArticles.page';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

		} catch (Exception e) {
			message = "<script>";
			message += " alert('작업중 오류가 발생했습니다.다시 시도해 주세요.');";
			message += " location.href='" + request.getContextPath() + "/H/H_P001/listArticles.page';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			e.printStackTrace();
		}
		return resEnt;
	}

	@RequestMapping(value = "H/H_P001/articleForm.user", method = RequestMethod.GET)
	private ModelAndView form(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewName = "/H_P001/articleForm.tiles";
		ModelAndView mav = new ModelAndView();
		mav.setViewName(viewName);
		return mav;
	}

	private String upload(MultipartHttpServletRequest multipartRequest) throws Exception {
		String imageFileName = null;
		Iterator<String> fileNames = multipartRequest.getFileNames();

		while (fileNames.hasNext()) {
			String fileName = fileNames.next();
			System.out.println("===========>> fileName:"+fileName);
			MultipartFile mFile = multipartRequest.getFile(fileName);
			imageFileName = mFile.getOriginalFilename();
			File file = new File(ARTICLE_IMAGE_REPO+ "\\" + "temp", imageFileName);
			System.out.println("===========>> new File:"+ARTICLE_IMAGE_REPO + "\\" + imageFileName);
			if (mFile.getSize() != 0) { // File Null Check
				if (!file.exists()) { // 경로상에 파일이 존재하지 않을 경우
					if (file.getParentFile().mkdirs()) { // 경로에 해당하는 디렉토리들을 생성
						file.createNewFile(); // 이후 파일 생성
					}
				}
				System.out.println("===========>> imageFileName:"+imageFileName);
				//mFile.transferTo(new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName)); // 임시로 저장된 multipartFile을 실제 파일로 전송
				mFile.transferTo(file); // 임시로 저장된 multipartFile을 실제 파일로 전송
			}
		}
		return imageFileName;
	}


}
