package entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * @author Administrator
 * @param <T>
 */
public class PageResult<T> implements Serializable {
	private static final long serialVersionUID = -8744309372650296153L;
	
	private Long total;
	private List<T> rows;
	
	public PageResult() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PageResult(Long total, List<T> rows) {
		super();
		this.total = total;
		this.rows = rows;
	}
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public List<T> getRows() {
		return rows;
	}
	public void setRows(List<T> rows) {
		this.rows = rows;
	}
	@Override
	public String toString() {
		return "PageResult [total=" + total + ", rows=" + rows + "]";
	}
	
}
