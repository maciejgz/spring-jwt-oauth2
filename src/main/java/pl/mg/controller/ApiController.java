package pl.mg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import pl.mg.exception.RestError;
import pl.mg.model.Account;
import pl.mg.model.RestResponse;
import pl.mg.service.AccountService;

import javax.security.auth.login.AccountException;

@RestController
public class ApiController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/api/hello")
    public ResponseEntity<?> hello() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        String msg = String.format("Hello %s", name);
        return new ResponseEntity<Object>(msg, HttpStatus.OK);
    }

    @GetMapping(path = "/api/me", produces = "application/json")
    public Account me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.findAccountByUsername(username);
    }

    @PostMapping(path = "/api/register", produces = "application/json")
    public ResponseEntity<?> register(@RequestBody Account account) {
        try {
            account.grantAuthority("ROLE_USER");
            return new ResponseEntity<Object>(
                    accountService.register(account), HttpStatus.OK);
        } catch (AccountException e) {
            e.printStackTrace();
            return new ResponseEntity<RestError>(new RestError(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(path = "/api/user/remove", produces = "application/json")
    public ResponseEntity<?> removeUser() {
        try {
            accountService.removeAuthenticatedAccount();
            return new ResponseEntity<Object>(new RestResponse("User removed."), HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<Object>(new RestError(e.getMessage()), HttpStatus.OK);
        }
    }

}
