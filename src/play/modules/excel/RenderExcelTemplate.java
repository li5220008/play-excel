package play.modules.excel;

import java.io.InputStream;
import java.util.Map;

import net.sf.jxls.transformer.XLSTransformer;

import org.apache.commons.codec.net.URLCodec;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import play.Play;
import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import play.vfs.VirtualFile;

/**
 * 200 OK with application/excel
 * 
 * This Result try to render Excel file with given template and beans map
 * The code use jxls and poi library to render Excel
 */
public class RenderExcelTemplate extends Result {
	
	private static final long serialVersionUID = 8823129782155149907L;

	private static URLCodec encoder = new URLCodec();
	private static VirtualFile tmplRoot = null;
	String templateName = null;
	String fileName = null; //recommended report file name
	boolean inline = true;
	Map<String, Object> beans = null;
	
	private static void initTmplRoot() {
		VirtualFile appRoot = VirtualFile.open(Play.applicationPath);
		tmplRoot = appRoot.child("app/views");
	}
	
	public RenderExcelTemplate(String templateName, Map<String, Object> beans) {
		this(templateName, beans, null);
	}

	public RenderExcelTemplate(String templateName, Map<String, Object> beans, String fileName) {
		this(templateName, beans, fileName, (null == fileName));
	}
	
	public RenderExcelTemplate(String templateName, Map<String, Object> beans, String fileName, boolean inline) {
		this.templateName = templateName;
		this.fileName = fileName;
		this.inline = inline;
		this.beans = beans;
	}

	@Override
	public void apply(Request request, Response response) {
		try {
            if (!response.headers.containsKey("Content-Disposition")) {
                if (inline) {
                    if (fileName == null) {
                        response.setHeader("Content-Disposition", "inline");
                    } else {
                        response.setHeader("Content-Disposition", "inline; filename*=utf-8''" + encoder.encode(fileName, "utf-8") + "; filename=\"" + encoder.encode(fileName, "utf-8") + "\"");
                    }
                } else if (fileName == null) {
                    response.setHeader("Content-Disposition", "attachment");
                } else {
                    response.setHeader("Content-Disposition", "attachment; filename*=utf-8''" + encoder.encode(fileName, "utf-8") + "; filename=\"" + encoder.encode(fileName, "utf-8") + "\"");
                }
            }
			setContentTypeIfNotSet(response, "application/excel");
            
    		if (null == tmplRoot) {
    			initTmplRoot();
    		}
    		InputStream is = tmplRoot.child(templateName).inputstream();
    		HSSFWorkbook workbook = new XLSTransformer().transformXLS(is, beans);
    		workbook.write(response.out);
    		is.close();
		} catch (Exception e) {
			throw new UnexpectedException(e);
		}
	}
}