package com.acme.salary.reference;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CountryRepository {

	private static final String SELECT_COUNTRY = """
			SELECT code, name, currency_code, units_per_usd, rate_as_of
			FROM country
			""";

	private final JdbcClient jdbc;

	public CountryRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public List<Country> findAll() {
		return jdbc.sql(SELECT_COUNTRY + " ORDER BY name").query(CountryRepository::mapRow).list();
	}

	public Map<String, Country> findAllByCode() {
		return findAll().stream().collect(Collectors.toMap(Country::code, Function.identity()));
	}

	public Optional<Country> findByCode(String code) {
		return jdbc.sql(SELECT_COUNTRY + " WHERE code = :code")
			.param("code", code)
			.query(CountryRepository::mapRow)
			.optional();
	}

	private static Country mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new Country(rs.getString("code"), rs.getString("name"), rs.getString("currency_code"),
				rs.getBigDecimal("units_per_usd"), LocalDate.parse(rs.getString("rate_as_of")));
	}

}
