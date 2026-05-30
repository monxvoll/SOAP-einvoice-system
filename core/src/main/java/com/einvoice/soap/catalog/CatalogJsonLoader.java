package com.einvoice.soap.catalog;

import com.einvoice.soap.gen.Customer;
import com.einvoice.soap.gen.Employee;
import com.einvoice.soap.gen.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
public class CatalogJsonLoader {

    private static final Logger log = LoggerFactory.getLogger(CatalogJsonLoader.class);

    private static final String CUSTOMERS_PATH = "data/customers.json";
    private static final String EMPLOYEES_PATH = "data/employees.json";
    private static final String PRODUCTS_PATH = "data/products.json";

    private static final int MIN_CUSTOMERS = 10;
    private static final int MIN_EMPLOYEES = 10;
    private static final int MIN_PRODUCTS = 15;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<Customer> customers = List.of();
    private List<Employee> employees = List.of();
    private List<Product> products = List.of();

    @PostConstruct
    void loadCatalogs() {
        customers = Collections.unmodifiableList(
                loadCustomers(CUSTOMERS_PATH, MIN_CUSTOMERS));
        employees = Collections.unmodifiableList(
                loadEmployees(EMPLOYEES_PATH, MIN_EMPLOYEES));
        products = Collections.unmodifiableList(
                loadProducts(PRODUCTS_PATH, MIN_PRODUCTS));
        log.info("Catálogos DIAN cargados: {} clientes, {} empleados, {} productos",
                customers.size(), employees.size(), products.size());
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Product> getProducts() {
        return products;
    }

    private List<Customer> loadCustomers(String path, int minimum) {
        try (InputStream input = openResource(path)) {
            List<CustomerJson> entries = objectMapper.readValue(input, new TypeReference<>() {});
            if (entries.size() < minimum) {
                throw new IllegalStateException(
                        "customers.json debe tener al menos " + minimum + " registros");
            }
            return entries.stream().map(this::toCustomer).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Error al parsear customers.json", e);
        }
    }

    private List<Employee> loadEmployees(String path, int minimum) {
        try (InputStream input = openResource(path)) {
            List<EmployeeJson> entries = objectMapper.readValue(input, new TypeReference<>() {});
            if (entries.size() < minimum) {
                throw new IllegalStateException(
                        "employees.json debe tener al menos " + minimum + " registros");
            }
            return entries.stream().map(this::toEmployee).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Error al parsear employees.json", e);
        }
    }

    private List<Product> loadProducts(String path, int minimum) {
        try (InputStream input = openResource(path)) {
            List<ProductJson> entries = objectMapper.readValue(input, new TypeReference<>() {});
            if (entries.size() < minimum) {
                throw new IllegalStateException(
                        "products.json debe tener al menos " + minimum + " registros");
            }
            return entries.stream().map(this::toProduct).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Error al parsear products.json", e);
        }
    }

    private InputStream openResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalStateException("Archivo de catálogo no encontrado: " + path);
        }
        return resource.getInputStream();
    }

    private Customer toCustomer(CustomerJson json) {
        Customer customer = new Customer();
        customer.setID(json.id);
        customer.setName(json.name);
        customer.setEmail(json.email);
        customer.setPhoneNumber(json.phoneNumber);
        return customer;
    }

    private Employee toEmployee(EmployeeJson json) {
        Employee employee = new Employee();
        employee.setID(json.id);
        employee.setName(json.name);
        employee.setPhoneNumber(json.phoneNumber);
        employee.setEmail(json.email);
        employee.setProductiveActivity(json.productiveActivity);
        return employee;
    }

    private Product toProduct(ProductJson json) {
        Product product = new Product();
        product.setID(json.id);
        product.setName(json.name);
        product.setUnitPrice(json.unitPrice);
        return product;
    }

    private static class CustomerJson {
        @JsonProperty("ID")
        public String id;
        @JsonProperty("Name")
        public String name;
        public String email;
        public String phoneNumber;
    }

    private static class EmployeeJson {
        @JsonProperty("ID")
        public String id;
        @JsonProperty("Name")
        public String name;
        public String phoneNumber;
        public String email;
        public String productiveActivity;
    }

    private static class ProductJson {
        @JsonProperty("ID")
        public String id;
        public String name;
        public String unitPrice;
    }
}
