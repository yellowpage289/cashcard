package com.sgg.cashcard;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cashcard")
class CashCardController {
	private final CashCardRepository cashCardRepository;
	
    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
	}

	@GetMapping("/{requestedId}")
	private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
		Optional<CashCard> cashCardOptional = Optional.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
		
 	    if (cashCardOptional.isPresent()) {
 	    	return ResponseEntity.ok(cashCardOptional.get());
 	    } else {
 	    	return ResponseEntity.notFound().build();
 	    }
	}

	@PostMapping
	private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
		CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
		CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb.path("cashcard/{id}").buildAndExpand(savedCashCard.id()).toUri();

		return ResponseEntity.created(locationOfNewCashCard).build();
	}

//	@GetMapping
//	private ResponseEntity<Iterable<CashCard>> findAll() {
//	   return ResponseEntity.ok(cashCardRepository.findAll());
//	}

	@GetMapping
	private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
	    Page<CashCard> page = cashCardRepository.findByOwner(
	    		principal.getName(),
	            PageRequest.of(
	                    pageable.getPageNumber(),
	                    pageable.getPageSize(),
	                    pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
	    ));
	    return ResponseEntity.ok(page.getContent());
	}

	@PutMapping("/{requestedId}")
	private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
		Optional<CashCard> targetCashCard = Optional.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
		if (targetCashCard.isPresent()) {
			CashCard updatedCashCard = new CashCard(targetCashCard.get().id(), cashCardUpdate.amount(), principal.getName());
			cashCardRepository.save(updatedCashCard);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{requestedId}")
	private ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal) {
		if (cashCardRepository.existsByIdAndOwner(requestedId, principal.getName())) {
			cashCardRepository.deleteById(requestedId);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

}
