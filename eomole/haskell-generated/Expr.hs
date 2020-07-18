module Expr where

inc x = x + 1
dec x = x - 1
add x y = x + y
mul x y = x * y
div x y = x / y
eq x y = x == y
lt x y = x < y
mod x = undefined -- TODO
dem x = undefined -- TODO
send x = undefined -- TODO
ap f x = f x
neg x = -x
s x y z = x z (y z)
c x y z = x z y
b x y z = x (y z)
t x _ = x
f _ y = y
pwr2 x = 2 ^ x
i x = x
cons x y f = f x y
car x = x t
cdr x = x f
nil x = t
isnil x = case undefined of -- TODO
    [] -> t
    x : xs -> f
draw x = undefined -- TODO
checkerboard x = undefined -- TODO
multipledraw x = undefined -- TODO
if0 0 y _ = y
if0 _ _ z = z
interact x = undefined -- TODO
