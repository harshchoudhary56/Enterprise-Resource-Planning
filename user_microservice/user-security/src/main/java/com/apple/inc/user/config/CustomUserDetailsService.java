//package com.apple.inc.user.config.jwt;
//
//import com.apple.inc.user.repository.jpa.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return userRepository.findByEmail(username)
//                .map(user -> new CustomUserDetails(user.getEmail(), user.getPassword()))
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
//    }
//}
