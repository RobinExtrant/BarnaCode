(define (problem BARNABOB)
(:domain BARNA)
(:objects C1 C2 C3 C4 C5 C6 C7 C8 C9 - case N S - but P1 P2 P3 P4 P5 P6 P7 P8 P9 - palet)
(:INIT (BUSY C1 P1) (BUSY C2 P2) (BUSY C3 P3) (BUSY C4 P4) (BUSY C5 P5) (BUSY C6 P6) (BUSY C7 P7) (BUSY C8 P8) (BUSY C9 P9) (HANDEMPTY) (ONBUT S))
(:goal (AND (BUSYBUT N P1) (BUSYBUT N P2) (BUSYBUT N P3) (BUSYBUT N P4) (BUSYBUT N P5) (BUSYBUT N P6) (BUSYBUT N P7) (BUSYBUT N P8) (BUSYBUT N P9)))
)
