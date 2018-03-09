;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Barnabob
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (domain BARNA)
  (:requirements :strips :typing)
  (:types case palet but)
  (:predicates (busy ?x - case ?y - palet)
	       (busybut ?x - but ?y - palet)
	       (on ?x - case)
	       (onbut ?x - but)
	       (holding ?x - palet)
	       (handempty)
	       )

  (:action movecase
	     :parameters (?x - case ?y - case)
	     :precondition (on ?x)
	     :effect 
	     (and (on ?y)
		  (not (on ?x))))

  (:action moveinbut
	     :parameters (?x - case ?y - but)
	     :precondition (on ?x)
	     :effect 
	     (and (onbut ?y)
		  (not (on ?x))))

  (:action moveoutbut
	     :parameters (?x - but ?y - case)
	     :precondition (onbut ?x)
	     :effect 
	     (and (on ?y)
		  (not (onbut ?x))))

  (:action pick-up
	     :parameters (?x - case ?y - palet)
	     :precondition (and (handempty) (busy ?x ?y) (on ?x)) 
	     :effect
	     (and (not (busy ?x ?y))
		   (holding ?y)
		   (not (handempty))))
		   
  (:action put-down
	     :parameters (?x - but ?y - palet)
	     :precondition (and (holding ?y) (onbut ?x)) 
	     :effect
	     (and (handempty)
		  (busybut ?x ?y)
		  (not (holding ?y)))))
