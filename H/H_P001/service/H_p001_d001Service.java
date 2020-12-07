package project.yoonju.H.H_P001.service;

import java.util.List;
import java.util.Map;

import project.yoonju.H.H_P001.vo.H_p001_d001VO;

//import com.myspring.pro30.board.vo.H_p001_d001VO;



public interface H_p001_d001Service {
	public List<H_p001_d001VO> listArticles() throws Exception;
	public String addNewArticle(Map articleMap) throws Exception;
	public H_p001_d001VO viewArticle(String post_num) throws Exception;
	//public Map viewArticle(int articleNO) throws Exception;
	//private String loginCheck(String user_id) throws Exception;
	public void modArticle(Map articleMap) throws Exception;
	public void removeArticle(String post_num) throws Exception;
	public int count() throws Exception;
	public List listPage(int displayPost, int postnum) throws Exception;
	public String addrplyArticle(Map articleMap) throws Exception;
}
