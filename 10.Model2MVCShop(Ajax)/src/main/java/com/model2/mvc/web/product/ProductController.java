package com.model2.mvc.web.product;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;


//==> 회원관리 Controller
@Controller
@RequestMapping("/product/*")
public class ProductController {
	
	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method 구현 않음
		
	public ProductController(){
		System.out.println(this.getClass());
	}
	
	//==> classpath:config/common.properties  ,  classpath:config/commonservice.xml 참조 할것
	//==> 아래의 두개를 주석을 풀어 의미를 확인 할것
	@Value("#{commonProperties['pageUnit']}")
	//@Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	//@Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;
	
	//local로 저장하는 위치 설정
	private static final String UPLOAD_PATH = "C:\\workspace\\09.Model2MVCShop(jQuery)\\WebContent\\images\\uploadFiles";
	
	
	@RequestMapping(value = "addProduct", method =RequestMethod.GET)
	public String addProduct() throws Exception {

		System.out.println("/product/addProduct : GET");
		
		return "redirect:/product/addProductView.jsp";
	}
	
	@RequestMapping(value = "addProduct", method = RequestMethod.POST)
	public String addProduct( @RequestParam("fileImage") ArrayList<MultipartFile> fileName,
								@ModelAttribute("product") Product product,
								Model model) throws Exception {
		
		//최종적으로 닮을 nullString 변수 선언
		String result = "";
		int i =0;
		
		System.out.println("/product/addProduct() POST start");
		
		for (MultipartFile files : fileName) {
			i++;
			result += saveFile(files);
			
			if(i != fileName.size()) {
				result += ":";
			}
        }

		 System.out.println("result : " + result);
		 
		 product.setFileName(result);
		 System.out.println("fileNameList : " + product.getFileNameList());
		 model.addAttribute("product", product);
		 System.out.println(product);
		 
		 
		 productService.addProduct(product);
		 return "forward:/product/addProduct.jsp";
		
	}
	
	private String saveFile(MultipartFile file) {
		
		//이름 중복되지 않게 파일이름 변경
		UUID uuid = UUID.randomUUID();
		//오리지날 파일이름에 고유식별자 + _ 을 붙여서 중복을 방지함
		String saveName = uuid + "_" + file.getOriginalFilename();
		
		System.out.println("File Save Name : " + saveName);
		
		File saveFile = new File(UPLOAD_PATH, saveName);
		
		try {
			file.transferTo(saveFile);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
		
		return saveName;
		
	}
	
	@RequestMapping(value = "getProduct", method = RequestMethod.GET)
	public String getProduct( @RequestParam("prodNo") int prodNo , 
			@RequestParam(value = "menu", required = false) String menu, 
			Model model ) throws Exception {
		
		System.out.println("prod_No : "  + prodNo );
		System.out.println("/product/getProduct : GET");
		//Business Logic
		Product product = productService.getProduct(prodNo);
		// Model 과 View 연결
		model.addAttribute("product", product);
		model.addAttribute("menu", menu);
		
		//쿠키구현은 @CookieValue를 사용해서 여기서 
		
		//parameter menu가 manage라면 
		if (menu.equals("manage")) {
			return "forward:/product/updateProductView.jsp";
		} else  {

			return "forward:/product/getProduct.jsp";
		}
		
	}
	
//	@RequestMapping("/updateProductView.do")
//	public String updateProductView( @RequestParam("prodNo") int prodNo , Model model ) throws Exception{
	@RequestMapping(value = "updateProduct", method = RequestMethod.GET)
	public String updateProduct( @RequestParam("prodNo") int prodNo , Model model ) throws Exception{

		System.out.println("/product/updateProduct : GET");
		//Business Logic
		Product product = productService.getProduct(prodNo);
		
		// Model 과 View 연결
		model.addAttribute("product", product);
		
		return "forward:/product/updateProduct.jsp";
	}
	
//	@RequestMapping("/updateProduct.do")
//	public String updateProduct( @ModelAttribute("product") Product product , Model model , HttpSession session) throws Exception{
	@RequestMapping(value = "updateProduct", method = RequestMethod.POST)
	public String updateProduct( @ModelAttribute("product") Product product , Model model , HttpSession session) throws Exception{

		System.out.println("/product/updateProduct : POST");
		//Business Logic
		productService.updateProduct(product);
		
//		int sessionNo=((Product)session.getAttribute("product")).getProdNo();
//		if(sessionNo == product.getProdNo()){
//			session.setAttribute("product", product);
//			System.out.println("updateProduct sessionNo 확인" + session.getAttribute("product"));
//		}
		model.addAttribute("product",product);
		System.out.println("product update : " + product);
		
		return "redirect:/product/getProduct?prodNo=" + product.getProdNo() + "&menu=ok";
	}
	
//	@RequestMapping("/listProduct.do")
//	public String listProduct( @ModelAttribute("search") Search search ,
//								@ModelAttribute("page") Page page ,
//								Model model , HttpServletRequest request) throws Exception{
	@RequestMapping(value = "listProduct")
	public String listProduct( @ModelAttribute("search") Search search ,
//			@ModelAttribute("page") Page page ,
			Model model , HttpServletRequest request) throws Exception{
		
		System.out.println("/product/listProduct GET / POST");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		
		search.setSearchCondition(request.getParameter("searchCondition"));
		search.setSearchKeyword(request.getParameter("searchKeyword"));
		search.setPageSize(pageSize);
		//아래 보여줄 pageUnit을 세팅해줬음. 페지이 모델어트리뷰트 추가
//		page.setPageUnit(pageUnit);
		
		
		// Business logic 수행
		Map<String , Object> map=productService.getProductList(search);
		System.out.println("map : " + map);
		
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		
		return "forward:/product/listProduct.jsp";
	}
}