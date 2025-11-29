package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Account;
import com.example.shopgaubong.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class AccountDAO extends BaseDAO<Account, Long> {

    public AccountDAO() {
        super(Account.class);
    }

    /**
     * Tìm Account theo username
     */
    public Optional<Account> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.username = :username";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            query.setParameter("username", username);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Account theo vai trò
     */
    public List<Account> findByRole(Role role) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.role = :role";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            query.setParameter("role", role);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Account đang hoạt động
     */
    public List<Account> findActiveAccounts() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Account a WHERE a.isActive = true";
            TypedQuery<Account> query = em.createQuery(jpql, Account.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    public boolean existsByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(a) FROM Account a WHERE a.username = :username";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật trạng thái tài khoản
     */
    public void updateAccountStatus(Long accountId, boolean isActive) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Account account = em.find(Account.class, accountId);
            if (account != null) {
                account.setIsActive(isActive);
                em.merge(account);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}

