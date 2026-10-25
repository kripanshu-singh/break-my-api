/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package me.kripanshu.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.kripanshu.backend.entity.Project;

/**
 *
 * @author home
 */
public interface ProjectRepository extends JpaRepository<Project, String> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
    boolean existsByBaseUrl(String baseUrl);
    boolean existsByBaseUrlAndIdNot(String baseUrl, String id);
}
