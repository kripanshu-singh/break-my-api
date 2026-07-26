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
    Optional<Project> findByName(String name);
}
