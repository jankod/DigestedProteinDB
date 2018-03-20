package hr.pbf.digestdb.web;

public class Pagination {
	/** The first page. */
	private static final int FIRST_PAGE = 1;

	/**
	 * Convert a string to an integer.
	 * 
	 * @param str
	 *            Your string.
	 * @param defaultValue
	 *            Default value when failed.
	 * @return A legal integer to use in this {@code Pagination}.
	 */
	private static int toLegalInt(String str, int defaultValue) {
		int i;
		try {
			i = Integer.parseInt(str, 10);
			if (i > 0)
				return i;
		} catch (Exception e) {
		}
		return defaultValue;
	}

	/** Current page. */
	private int currentPage;

	/** Page size, amount of entries showing per page. */
	private int size;

	/** Amount of entries. */
	private int total;

	/** Amount of pages. */
	private int numberOfPages;

	/**
	 * Create a pagination, page size would be set to 10.
	 * 
	 * @param page
	 *            Current page number you got from front end, 1 if illegal.
	 * @param total
	 *            Total amount of entries.
	 */
	public Pagination(String page, int total) {
		this(page, total, 10);
	}

	/**
	 * Create a pagination.
	 * 
	 * @param page
	 *            Current page number you got from front end, 1 if illegal.
	 * @param total
	 *            Total amount of entries.
	 * @param size
	 *            Amount of entries to show per page, 10 if non-positive.
	 */
	public Pagination(String page, int total, int size) {
		this.currentPage = toLegalInt(page, 1);
		this.total = total;
		this.size = size > 0 ? size : 10;
		init();
	}

	/**
	 * Create a pagination.
	 * 
	 * @param page
	 *            Current page number you got from front end, 1 if illegal.
	 * @param total
	 *            Total amount of entries.
	 * @param size
	 *            Amount of entries to show per page, 10 if illegal.
	 */
	public Pagination(String page, int total, String size) {
		this.currentPage = toLegalInt(page, 1);
		this.total = total;
		this.size = toLegalInt(size, 10);
		init();
	}

	/**
	 * Returns current page number.
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * Returns first page. Should always be 1.
	 */
	public int getFirstPage() {
		return FIRST_PAGE;
	}

	/**
	 * Get index of the first entry in this page. You can use this value in DB like
	 * <p>
	 * {@code select * from TABLE_NAME limit [index], 10} (MariaDB),
	 * </p>
	 * <p>
	 * {@code db.COLLECTION_NAME.find().skip([index]).limit(10)} (MongoDB).
	 * </p>
	 */
	public int getIndex() {
		return size * (currentPage - 1);
	}

	/**
	 * Returns the last page.
	 */
	public int getLastPage() {
		return numberOfPages;
	}

	/**
	 * Returns the next page number.
	 */
	public int getNextPage() {
		return (currentPage < numberOfPages) ? (currentPage + 1) : numberOfPages;
	}

	/**
	 * Returns total of pages.
	 */
	public int getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * Returns the previous page number.
	 */
	public int getPrevPage() {
		return (currentPage > 1) ? (currentPage - 1) : 1;
	}

	/**
	 * Returns the page size, namely, it is the amount of entries showing per page.
	 * You can use this value in DB like
	 * <p>
	 * {@code select * from TABLE_NAME limit 0, [size]} (MariaDB),
	 * </p>
	 * <p>
	 * {@code db.COLLECTION_NAME.find().skip(0).limit([size])} (MongoDB).
	 * </p>
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns total of entries.
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * Initialize this pagination, handle some abnormal conditions.
	 */
	private void init() {
		if (total > 0) {
			numberOfPages = total / size + ((total % size == 0) ? 0 : 1);
		} else {
			numberOfPages = 1; // At least one page.
		}
		if (currentPage > numberOfPages) { // Exceeded!
			currentPage = numberOfPages;
		}
	}

	/**
	 * Returns true if current page is the last.
	 */
	public boolean isLastPage() {
		return currentPage == numberOfPages;
	}

	/**
	 * Returns a string representation of this {@code Pagination}.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(98);
		sb.append('[');
		sb.append(FIRST_PAGE);
		sb.append(" << ");
		sb.append(getPrevPage());
		sb.append(" < ");
		sb.append(currentPage);
		sb.append(" > ");
		sb.append(getNextPage());
		sb.append(" >> ");
		sb.append(numberOfPages);
		sb.append(" | showing ");
		if (isLastPage()) {
			sb.append(total - getIndex());
		} else {
			sb.append(size);
		}
		sb.append(" of ");
		sb.append(total);
		sb.append(" records]");
		return sb.toString();
	}
}
