package com.model2.mvc.web.product;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.product.impl.ProductServiceImpl;


//==> 회원관리 Controller
@RestController
@RequestMapping("/product/*")
public class ProductRestController {
	
	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method 구현 않음
		
	public ProductRestController(){
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
	
	//.jsp로 안내할 일이 없어서 GET방식은 삭제
//	@RequestMapping(value = "addProduct", method =RequestMethod.GET)
//	public String addProduct() throws Exception {
//
//		System.out.println("/product/addProduct : GET");
//		
//		return "redirect:/product/addProductView.jsp";
//	}
	
	//아직 안함
	@RequestMapping(value = "json/addProduct", method = RequestMethod.POST)
	public String addProduct( @ModelAttribute("product") Product product,
								HttpServletRequest request,
								HttpServletResponse response,
								Model model) throws Exception {
		
		if(FileUpload.isMultipartContent(request)) {
			String temDir = "C:\\Users\\user\\git\\repository\\07PJTmini\\07.Model2MVCShop(URI,pattern)\\WebContent\\images\\uploadFiles";
			
			DiskFileUpload fileUpload = new DiskFileUpload();
			fileUpload.setRepositoryPath(temDir);
			fileUpload.setSizeMax(1024*1024*50);
			//gksqjsdp 100k까지는 메모리에 저장
			fileUpload.setSizeThreshold(1024*100);
			
			if(request.getContentLength() < fileUpload.getSizeMax()) {
//				Product productImage = new Product();
				
				StringTokenizer token = null; 
				
				List fileItemList = fileUpload.parseRequest(request);
				int Size = fileItemList.size();
				for (int i = 0; i < Size; i++) {
					FileItem fileItem = (FileItem)fileItemList.get(i);
					//isFormField()를 통해서 파일형식인지 파라미터인지 구분한다.
					//파라미터라면 true
					if(fileItem.isFormField()) {
						if (fileItem.getFieldName().equals("manuDate")) {
							token = new StringTokenizer(fileItem.getString("euc-kr"),"-" );
							String manuDate = token.nextToken() + token.nextToken() + 
									token.nextToken();
							product.setManuDate(manuDate);
						}
						else if(fileItem.getFieldName().equals("prodName")) 
							product.setProdName(fileItem.getString("euc-kr"));
						else if(fileItem.getFieldName().equals("prodDetail")) 
							product.setProdDetail(fileItem.getString("euc-kr"));
						else if(fileItem.getFieldName().equals("price")) 
							product.setPrice(Integer.parseInt(fileItem.getString("euc-kr")));
						
					}else {
						
						if(fileItem.getSize() > 0) {
							int idx = fileItem.getName().lastIndexOf("\\");
							
							if(idx == -1) {
								idx = fileItem.getName().lastIndexOf("/");
							}
							String fileName = fileItem.getName().substring(idx + 1);
							product.setFileName(fileName);
							try {
								File uploadedFile = new File(temDir,fileName);
								fileItem.write(uploadedFile);
							}catch(IOException e) {
								System.out.println(e);
							}
						}else {
							product.setFileName("../../images/empty.GIF");
						}
					}//else
				}//for
				
				productService.addProduct(product);
				
				request.setAttribute("product", product);
				model.addAttribute("product", product);
				
			}else {
				int overSize = (request.getContentLength() / 1000000);
				System.out.println("<script>alert('파일의 크기는 1MB까지 입니다. 올리신 파일 용량은" + overSize + "MB입니다');");
				System.out.println("history.back(); </script>");
			}
		}else {
			System.out.println("인코딩 타입이 multipart/form-data가 아닙니다.");
		}
		
		//사진 업로드시 리턴 위치. 이걸 어떻게..?
//		return "forward:/product/getProduct.jsp";
		
		System.out.println("/product/addProduct : POST");
		//Business Logic
//		productService.addProduct(product);
		
		//담긴 정보를 넘기기위해서 redirect에서 forward로 변경
		return "forward:/product/addProduct.jsp";
	}
	
	@RequestMapping(value = "json/getProduct/{prodNo}", method = RequestMethod.GET)
	public Product getProduct( @PathVariable int prodNo) throws Exception {
		
		System.out.println("/product/getProduct/{prodNo} : GET ");
		return productService.getProduct(prodNo);
		
		// Model 과 View 연결
		
		//쿠키구현은 @CookieValue를 사용해서 여기서 
	}
	
	@RequestMapping(value = "json/updateProduct/{prodNo}", method = RequestMethod.GET)
	public Product updateProduct( @PathVariable("prodNo") int prodNo ) throws Exception{

		System.out.println("json/product/updateProduct : GET");
		
		//Business Logic
		return productService.getProduct(prodNo);
		
	}
	
	@RequestMapping(value = "json/updateProduct", method = RequestMethod.POST)
	public void updateProduct( @RequestBody Product product ) throws Exception{

		System.out.println("/product/json/updateProduct : POST");
		//Business Logic
		productService.updateProduct(product);
		
		System.out.println("/product/json/updateProduct : POST 끝");
		
	}
	
	@RequestMapping(value = "json/listProduct", method = RequestMethod.GET)
	public Map listProduct( HttpServletRequest request) throws Exception{
		
		System.out.println("json/listProduct GET start");
		
		
		Search search = new Search();
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		//아래 보여줄 pageUnit을 세팅해줬음. 페지이 모델어트리뷰트 추가
		
		
		// Business logic 수행
		Map<String , Object> map=productService.getProductList(search);
		System.out.println("map : " + map);
		
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		map.put("list", map.get("list"));
		map.put("resultPage", resultPage);
		map.put("search", search);
		
		System.out.println("json/listProduct GET end");
		return map;		
	}
}