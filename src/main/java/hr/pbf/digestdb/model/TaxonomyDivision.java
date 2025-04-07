package hr.pbf.digestdb.model;

import lombok.Getter;

@Getter
public enum TaxonomyDivision {
	BACTERIA(0, "BCT", "Bacteria"),
	INVERTEBRATES(1, "INV", "Invertebrates"),
	MAMMALS(2, "MAM", "Mammals"),
	PHAGES(3, "PHG", "Phages"),
	PLANTS_AND_FUNGI(4, "PLN", "Plants and Fungi"),
	PRIMATES(5, "PRI", "Primates"),
	RODENTS(6, "ROD", "Rodents"),
	SYNTHETIC_AND_CHIMERIC(7, "SYN", "Synthetic and Chimeric"),
	UNASSIGNED(8, "UNA", "Unassigned"),
	VIRUSES(9, "VRL", "Viruses"),
	VERTEBRATES(10, "VRT", "Vertebrates"),
	ENVIRONMENTAL_SAMPLES(11, "ENV", "Environmental samples"),
	ALL(-1, "ALL", "All");

	private final int id;
	private final String code;
	private final String name;

	TaxonomyDivision(int id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public static TaxonomyDivision fromId(int id) {
		for(TaxonomyDivision division : TaxonomyDivision.values()) {
			if(division.id == id) {
				return division;
			}
		}
		throw new IllegalArgumentException("No constant with id " + id + " found");
	}

	public static TaxonomyDivision fromCode(String code) {
		for(TaxonomyDivision division : TaxonomyDivision.values()) {
			if(division.code.equalsIgnoreCase(code)) {
				return division;
			}
		}
		throw new IllegalArgumentException("No constant with code " + code + " found");
	}

	public static TaxonomyDivision fromName(String name) {
		for(TaxonomyDivision division : TaxonomyDivision.values()) {
			if(division.name.equalsIgnoreCase(name)) {
				return division;
			}
		}
		throw new IllegalArgumentException("No constant with name " + name + " found");
	}
}