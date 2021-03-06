/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2016 the original author or authors.
 */
package org.assertj.core.groups;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.extractor.Extractors.byName;
import static org.assertj.core.groups.FieldsOrPropertiesExtractor.extract;
import static org.assertj.core.test.ExpectedException.none;
import static org.assertj.core.util.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.test.Employee;
import org.assertj.core.test.ExpectedException;
import org.assertj.core.test.Name;
import org.assertj.core.util.introspection.IntrospectionError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class FieldsOrPropertiesExtractor_extract_Test {
  
  @Rule
  public ExpectedException thrown = none();
  
  private Employee yoda;
  private Employee luke;
  private List<Employee> employees;

  @Before
  public void setUp() {
    yoda = new Employee(1L, new Name("Yoda"), 800);
    yoda.surname = new Name("Master", "Jedi");
    luke = new Employee(2L, new Name("Luke", "Skywalker"), 26);
    employees = newArrayList(yoda, luke);
  }

  @Test
  public void should_extract_field_values_in_absence_of_properties() {
    List<Object> extractedValues = extract(employees, byName("id"));
    assertThat(extractedValues).containsOnly(1L, 2L);
  }
  
  @Test
  public void should_extract_null_valuesfor_null_property_values() {
    yoda.setName(null);
    List<Object> extractedValues = extract(employees, byName("name"));
    assertThat(extractedValues).containsOnly(null, new Name("Luke", "Skywalker"));
  }
  
  @Test
  public void should_extract_null_values_for_null_nested_property_values() {
    yoda.setName(null);
    List<Object> extractedValues = extract(employees, byName("name.first"));
    assertThat(extractedValues).containsOnly(null, "Luke");
  }
  
  @Test
  public void should_extract_null_valuesfor_null_field_values() {
    List<Object> extractedValues = extract(employees, byName("surname"));
    assertThat(extractedValues).containsOnly(new Name("Master", "Jedi"), null);
  }
  
  @Test
  public void should_extract_null_values_for_null_nested_field_values() {
    List<Object> extractedValues = extract(employees, byName("surname.first"));
    assertThat(extractedValues).containsOnly("Master", null);
  }
  
  @Test
  public void should_extract_property_values_when_no_public_field_match_given_name() {
    List<Object> extractedValues = extract(employees, byName("age"));
    assertThat(extractedValues).containsOnly(800, 26);
  }
  
  @Test
  public void should_extract_pure_property_values() {
    List<Object> extractedValues = extract(employees, byName("adult"));
    assertThat(extractedValues).containsOnly(true);
  }
  
  @Test
  public void should_throw_error_when_no_property_nor_public_field_match_given_name() {
    thrown.expect(IntrospectionError.class);
    extract(employees, byName("unknown"));
  }
  
  @Test
  public void should_throw_exception_when_given_name_is_null() {
    thrown.expectIllegalArgumentException("The name of the field/property to read should not be null");
    extract(employees, byName((String)null));
  }
  
  @Test
  public void should_throw_exception_when_given_name_is_empty() {
    thrown.expectIllegalArgumentException("The name of the field/property to read should not be empty");
    extract(employees, byName(""));
  }
  
  @Test
  public void should_fallback_to_field_if_exception_has_been_thrown_on_property_access() throws Exception {

    List<Employee> employees = Arrays.<Employee>asList(employeeWithBrokenName("Name"));
    List<Object> extractedValues = extract(employees, byName("name"));
    assertThat(extractedValues).containsOnly(new Name("Name"));
  }


  @Test
  public void should_prefer_properties_over_fields() throws Exception {
    
    List<Employee> employees = Arrays.<Employee>asList(employeeWithOverriddenName("Overridden Name"));
    List<Object> extractedValues = extract(employees, byName("name"));
    assertThat(extractedValues).containsOnly(new Name("Overridden Name"));
  }

  @Test
  public void should_throw_exception_if_property_cannot_be_extracted_due_to_runtime_exception_during_property_access() throws Exception {
    
    thrown.expect(IntrospectionError.class);
    
    List<Employee> employees = Arrays.<Employee>asList(brokenEmployee());
    extract(employees, byName("adult"));
  }

  // --
  
  private Employee employeeWithBrokenName(String name) {
    return new Employee(1L, new Name(name), 0){
      
      @Override
      public Name getName() {
        throw new IllegalStateException();
      }
    };
  }
  
  private Employee employeeWithOverriddenName(final String overriddenName) {
    return new Employee(1L, new Name("Name"), 0){
      
      @Override
      public Name getName() {
        return new Name(overriddenName);
      }
    };
  }

  private Employee brokenEmployee() {
    return new Employee(){
      
      @Override
      public boolean isAdult() {
        throw new IllegalStateException();
      }
    };
  }
}
