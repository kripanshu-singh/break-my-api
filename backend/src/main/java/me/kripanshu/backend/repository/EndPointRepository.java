/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package me.kripanshu.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.kripanshu.backend.entity.Endpoint;

/**
 *
 * @author home
 */
public interface EndPointRepository extends JpaRepository<Endpoint, String> {
    List<Endpoint> findByProjectId(String projectId);
}
