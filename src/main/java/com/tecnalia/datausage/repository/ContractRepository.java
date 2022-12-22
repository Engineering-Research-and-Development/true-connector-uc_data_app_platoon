/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tecnalia.datausage.model.ContractStore;

/**
 *
 * @author root
 */
@Repository
public interface ContractRepository extends JpaRepository<ContractStore, Long> {
	Optional<ContractStore> findByContractId(String contractId);

	Optional<ContractStore> findByContractUuid(String contractUuid);

	Long deleteByContractUuid(String contractUuid);

	Iterable<ContractStore> findAllByProviderIdAndConsumerId(String providerId, String consumerId);

}