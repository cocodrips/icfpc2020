fn modulate(s: &[i64]) -> String {
    match s {
        [] => "00".to_string(), // nil
        [head, tail @ ..] => format!("11{}{}", modulate_int(*head), modulate(tail)),
    }
}

fn modulate_int(s: i64) -> String {
    if s == 0 {
        return "010".to_string();
    }
    if s < 0 {
        return modulate_int(-s);
    }

    let mut res = "01".to_string();

    let n_bits = 64 - s.leading_zeros() as usize;
    let n_4bits = (n_bits + 3) / 4;

    for _ in 0..n_4bits {
        res.push('1');
    }
    res.push('0');

    let bits = format!("{:064b}", s);
    for c in bits.chars().skip(64 - 4 * n_4bits) {
        res.push(c);
    }

    res
}

fn main() {
    use std::io::BufRead;
    let stdin = std::io::stdin();
    let stdin = stdin.lock();
    let line = stdin.lines().take(1).next().unwrap().unwrap();
    let tokens = line.trim().split_whitespace();
    let ints = tokens
        .map(|t| t.parse::<i64>().unwrap())
        .collect::<Vec<_>>();
    println!("{}", modulate(&ints));
}
