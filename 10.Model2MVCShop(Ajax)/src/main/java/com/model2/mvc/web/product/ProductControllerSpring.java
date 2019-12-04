package com.model2.mvc.web.product;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.product.impl.ProductServiceImpl;


//==> 회원관리 Controller
//@Controller
@RequestMapping("/product/*")
public class ProductControllerSpring {
	
	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method 구현 않음
		
	public ProductControllerSpring(){
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
	
	
//	@RequestMapping("/addProductView.do")
//	public String addProductView() throws Exception {
	@RequestMapping(value = "addProduct", method =RequestMethod.GET)
	public String addProduct() throws Exception {

		System.out.println("/product/addProduct : GET");
		
		return "redirect:/product/addProductView.jsp";
	}
	
	@RequestMapping(value = "addProduct", method = RequestMethod.POST)
	public String addProduct( @ModelAttribute("product") Product product,
								MultipartHttpServletRequest mtfRequest,
								Model model) throws Exception {
		System.out.println("product : " + product);
		List<MultipartFile> fileList = mtfRequest.getFiles("fileName2");
		
		System.out.println("fileList value : " + fileList);

		String path = "C:\\workspace\\09.Model2MVCShop(jQuery)\\WebContent\\images\\uploadFiles";
			
		
		 for (MultipartFile mf : fileList) {
			 
	            product.setFileName(mf.getOriginalFilename()); // 원본 파일 명
	            long fileSize = mf.getSize(); // 파일 사이즈

	            System.out.println("originFileName : " + product.getFileName());
	            System.out.println("fileSize : " + fileSize);

	            String safeFile = path + System.currentTimeMillis() + product.getFileName() + ",";
	            try {
	                mf.transferTo(new File(safeFile));
	            } catch (IllegalStateException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }

		 System.out.println("/product/addProduct : POST");
		 
		 model.addAttribute("product", product);
		 
		 
		 productService.addProduct(product);
		 return "forward:/product/addProduct.jsp";
		
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