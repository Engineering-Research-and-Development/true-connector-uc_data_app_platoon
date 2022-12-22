/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.repository;

import com.tecnalia.datausage.model.AccessStore;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author root
 */
@Repository
public interface AccessRepository extends JpaRepository<AccessStore, Long> {
	Optional<AccessStore> findByConsumerUriAndTargetUri(String consumerUri, String targetUri);

}