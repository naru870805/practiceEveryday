package project.yoonju.H.H_P001.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import project.yoonju.H.H_P001.vo.H_p001_d001VO;
import project.yoonju.H.H_P001.vo.ImageVO;

/*import com.myspring.pro30.board.vo.H_p001_d001VO;
import com.myspring.pro30.board.vo.ImageVO;*/




@Repository("boardDAO")
public class H_p001_d001DAOImpl implements H_p001_d001DAO {
	@Autowired
	private SqlSession sqlSession;

	@Override
	public List selectAllArticlesList() throws DataAccessException {
		List<H_p001_d001VO> articlesList = sqlSession.selectList("yoonju.H.H_P001.H_p001_d001DAO.selectAllArticlesList");
		System.out.println("DAO=========>" + articlesList.size());
		System.out.println("DAO=========>" + articlesList.isEmpty());
		System.out.println("DAO=========>" + articlesList.toString());
		return articlesList;
	}

	
	@Override
	public String insertNewArticle(Map articleMap) throws DataAccessException {
		String post_num = selectNewArticleNO();
		articleMap.put("post_num", post_num);
		sqlSession.insert("yoonju.H.H_P001.H_p001_d001DAO.insertNewArticle",articleMap);
		return post_num;
	}
    
	@Override
	public String addrplyArticle(Map articleMap) throws DataAccessException {
		String parent_num = (String)articleMap.get("post_num");
		articleMap.put("parent_num", parent_num);
		sqlSession.insert("yoonju.H.H_P001.H_p001_d001DAO.addrplyArticle",articleMap);
		return null;
	}
	
	@Override
	public H_p001_d001VO selectArticle(String post_num) throws DataAccessException {
		return sqlSession.selectOne("yoonju.H.H_P001.H_p001_d001DAO.selectArticle", post_num);
	}

	@Override
	public void updateArticle(Map articleMap) throws DataAccessException {
		sqlSession.update("yoonju.H.H_P001.H_p001_d001DAO.updateArticle", articleMap);
	}

	@Override
	public void deleteArticle(String post_num) throws DataAccessException {
		sqlSession.delete("yoonju.H.H_P001.H_p001_d001DAO.deleteArticle", post_num);
		
	}
	
	@Override
	public List selectImageFileList(String post_num) throws DataAccessException {
		List<ImageVO> imageFileList = null;
		imageFileList = sqlSession.selectList("yoonju.H.H_P001.H_p001_d001DAO.selectImageFileList",post_num);
		return imageFileList;
	}
	
	private String selectNewArticleNO() throws DataAccessException {
		return sqlSession.selectOne("yoonju.H.H_P001.H_p001_d001DAO.selectNewArticleNO");
	}
	
	private String selectNewImageFileNO() throws DataAccessException {
		return sqlSession.selectOne("yoonju.H.H_P001.H_p001_d001DAO.selectNewImageFileNO");
	}
	
	@Override
	public int count() throws Exception{
		return sqlSession.selectOne("yoonju.H.H_P001.H_p001_d001DAO.count");
	}
	
	public List listPage(int displayPost, int postnum) throws DataAccessException{
		HashMap data = new HashMap();
		System.out.println("디스플레이포스트는?--------------->" + displayPost);
		System.out.println("포스트넘은?------------------>" + postnum);
		data.put("displayPost", displayPost);
		data.put("postnum", postnum);
		
		return sqlSession.selectList("yoonju.H.H_P001.H_p001_d001DAO.listPage", data);
	}


}
